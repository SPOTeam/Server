package com.example.spot.service.memberstudy;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.MemberReport;
import com.example.spot.domain.Notification;
import com.example.spot.domain.Quiz;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.mapping.*;
import com.example.spot.domain.study.*;
import com.example.spot.repository.*;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.s3.S3ImageService;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.memberstudy.request.*;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListRequestDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListRequestDTO.ToDoListCreateDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListCreateResponseDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListUpdateResponseDTO;
import com.example.spot.web.dto.memberstudy.response.*;
import com.example.spot.web.dto.study.response.StudyApplyResponseDTO;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class MemberStudyCommandServiceImpl implements MemberStudyCommandService {

    @Value("${image.post.anonymous.profile}")
    private String defaultImage;

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final ScheduleRepository scheduleRepository;
    private final QuizRepository quizRepository;
    private final VoteRepository voteRepository;
    private final OptionRepository optionRepository;
    private final MemberReportRepository memberReportRepository;
    private final StudyPostReportRepository studyPostReportRepository;

    private final MemberStudyRepository memberStudyRepository;
    private final MemberAttendanceRepository memberAttendanceRepository;
    private final StudyPostRepository studyPostRepository;
    private final MemberVoteRepository memberVoteRepository;
    private final ToDoListRepository toDoListRepository;
    private final NotificationRepository notificationRepository;

    // S3 Service
    private final S3ImageService s3ImageService;

/* ----------------------------- 진행중인 스터디 관련 API ------------------------------------- */

    /**
     * 진행중인 스터디에서 탈퇴하기 위한 메서드입니다.
     * 스터디장은 스터디를 탈퇴할 수 없으며 스터디를 종료하고자 하는 경우 스터디 terminateStudy API를 호출해야 합니다.
     * @param memberId 스터디를 탈퇴할 타겟 회원의 아이디(로그인 아이디 X)를 입력 받습니다.
     * @param studyId 타겟 회원이 탈퇴하고자 하는 스터디의 아이디를 입력 받습니다.
     * @return 탈퇴한 스터디의 아이디와 이름, 탈퇴한 회원의 아이디와 이름이 반환됩니다.
     */
    public StudyWithdrawalResponseDTO.WithdrawalDTO withdrawFromStudy(Long memberId, Long studyId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 참여가 승인되지 않은 스터디는 탈퇴할 수 없음
        if (memberStudy.getStatus().equals(ApplicationStatus.APPLIED)) {
            throw new StudyHandler(ErrorStatus._STUDY_NOT_APPROVED);
        }
        // 스터디장은 스터디를 탈퇴할 수 없음
        if (memberStudy.getIsOwned()) {
            throw new StudyHandler(ErrorStatus._STUDY_OWNER_CANNOT_WITHDRAW);
        }

        memberStudyRepository.delete(memberStudy);

        return StudyWithdrawalResponseDTO.WithdrawalDTO.toDTO(member, study);
    }

    /**
     * 운영중인 스터디를 종료하는 메서드입니다. 스터디장만 호출 가능합니다.
     * @param studyId 종료할 스터디의 아이디를 입력 받습니다.
     * @return 종료된 스터디의 아이디, 이름, 상태를 반환합니다.
     */
    public StudyTerminationResponseDTO.TerminationDTO terminateStudy(Long studyId) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 스터디장 확인 로직 빠짐

        study.setStatus(Status.OFF);
        studyRepository.save(study);

        return StudyTerminationResponseDTO.TerminationDTO.toDTO(study);
    }

    /**
     * 스터디 신청을 처리합니다. isAccept가 true이면 승인, false이면 거절합니다.
     * 이후 관련 알림을 생성합니다. 알림을 통해 최종 참여 승인을 해야 스터디에 참여할 수 있습니다.
     * @param memberId 스터디에 신청한 회원 ID
     * @param studyId 스터디 ID
     * @param isAccept 승인 여부
     * @return 스터디 신청 처리 결과 및 처리 시간
     * @throws GeneralException 스터디 신청을 처리하는 회원이 스터디 소유자가 아닐 때
     * @throws GeneralException 스터디 소유자가 신청한 경우
     * @throws StudyHandler 스터디 신청자를 찾을 수 없을 때
     * @throws StudyHandler 스터디 신청이 이미 처리되었을 때
     * @throws MemberHandler 스터디 장을 찾을 수 없을 때
     */
    @Override
    public StudyApplyResponseDTO acceptAndRejectStudyApply(Long memberId, Long studyId,
        boolean isAccept) {

        // 신청을 처리하는 회원이 스터디 소유자인지 확인
        if (!isOwner(SecurityUtils.getCurrentUserId(), studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_OWNER_CAN_ACCESS_APPLICANTS);

        // 스터디 신청자 조회
        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPLIED)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_APPLICANT_NOT_FOUND));

        // 스터디 소유자가 스터디 신청한 경우
        if (memberStudy.getIsOwned())
            throw new GeneralException(ErrorStatus._STUDY_OWNER_CANNOT_APPLY);

        // 스터디 신청이 이미 처리되었을 때
        if (memberStudy.getStatus() != ApplicationStatus.APPLIED)
            throw new GeneralException(ErrorStatus._STUDY_APPLY_ALREADY_PROCESSED);

        // 스터디 장 조회
        Member owner = memberRepository.findById(SecurityUtils.getCurrentUserId())
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 승인인 경우
        if (isAccept) {
            // 스터디 참여 승인 최종 대기
            memberStudy.setStatus(ApplicationStatus.AWAITING_SELF_APPROVAL);

            // 알림 생성
            Notification notification = Notification.builder()
                .member(memberStudy.getMember()) // 신청자
                .study(memberStudy.getStudy())
                .notifierName(owner.getName()) // 스터디장 이름
                .type(NotifyType.STUDY_APPLY)
                .isChecked(Boolean.FALSE)
                .build();

            notificationRepository.save(notification);
        }
        else { // 거절인 경우
            memberStudy.setStatus(ApplicationStatus.REJECTED);
            memberStudyRepository.delete(memberStudy);
        }

        // 스터디 신청 처리 결과 반환
        return StudyApplyResponseDTO.builder()
            .status(memberStudy.getStatus())
            .updatedAt(memberStudy.getUpdatedAt())
            .build();
    }

    /**
     * 스터디 신청을 처리합니다. isAccept가 true이면 승인, false이면 거절합니다.
     * 이 메서드를 사용하면 알림 처리 없이 바로 스터디에 참여할 수 있습니다.
     * @param memberId 스터디에 신청한 회원 ID
     * @param studyId 스터디 ID
     * @param isAccept 승인 여부
     * @return 스터디 신청 처리 결과 및 처리 시간
     * @throws GeneralException 스터디 신청을 처리하는 회원이 스터디 소유자가 아닐 때
     * @throws GeneralException 스터디 소유자가 신청한 경우
     * @throws StudyHandler 스터디 신청자를 찾을 수 없을 때
     * @throws StudyHandler 스터디 신청이 이미 처리되었을 때
     * @throws MemberHandler 스터디 장을 찾을 수 없을 때
     *
     */
    @Override
    public StudyApplyResponseDTO acceptAndRejectStudyApplyForTest(Long memberId, Long studyId,
        boolean isAccept) {

        // 스터디 신청을 처리하는 회원이 스터디 소유자인지 확인
        if (!isOwner(SecurityUtils.getCurrentUserId(), studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_OWNER_CAN_ACCESS_APPLICANTS);

        // 스터디 신청자 조회
        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPLIED)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_APPLICANT_NOT_FOUND));

        // 스터디 소유자가 스터디 신청한 경우
        if (memberStudy.getIsOwned())
            throw new GeneralException(ErrorStatus._STUDY_OWNER_CANNOT_APPLY);

        // 스터디 신청이 이미 처리되었을 때
        if (memberStudy.getStatus() != ApplicationStatus.APPLIED)
            throw new GeneralException(ErrorStatus._STUDY_APPLY_ALREADY_PROCESSED);

        // 스터디 장 조회
        Member owner = memberRepository.findById(SecurityUtils.getCurrentUserId())
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 승인인 경우
        if (isAccept) {
            memberStudy.setStatus(ApplicationStatus.APPROVED);
        }
        else {
            memberStudy.setStatus(ApplicationStatus.REJECTED);
            memberStudyRepository.delete(memberStudy);
        }

        // 스터디 신청 처리 결과 반환
        return StudyApplyResponseDTO.builder()
            .status(memberStudy.getStatus())
            .updatedAt(memberStudy.getUpdatedAt())
            .build();
    }

    /* ----------------------------- 스터디 일정 관련 API ------------------------------------- */

    @Override
    public ScheduleResponseDTO.ScheduleDTO addSchedule(Long studyId, ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        //=== Feature ===//
        Schedule schedule = Schedule.builder()
                .study(study)
                .member(member)
                .title(scheduleRequestDTO.getTitle())
                .location(scheduleRequestDTO.getLocation())
                .startedAt(scheduleRequestDTO.getStartedAt())
                .finishedAt(scheduleRequestDTO.getFinishedAt())
                .isAllDay(scheduleRequestDTO.getIsAllDay())
                .period(scheduleRequestDTO.getPeriod())
                .build();

        // 알림 생성

        // 스터디에 참여중인 회원들에게 알림 전송 위해 회원 조회
        List<Member> members = memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED).stream()
            .map(MemberStudy::getMember)
            .toList();


        if (members.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND);

        members.forEach(studyMember -> {
                Notification notification = Notification.builder()
                    .member(studyMember)
                    .study(study)
                    .notifierName(member.getName()) // 일정 생성자 이름
                    .type(NotifyType.SCHEDULE_UPDATE)
                    .isChecked(Boolean.FALSE)
                    .build();
                notificationRepository.save(notification);
            });

        scheduleRepository.save(schedule);
        study.addSchedule(schedule);
        member.addSchedule(schedule);

        return ScheduleResponseDTO.ScheduleDTO.toDTO(schedule);
    }

    @Override
    public ScheduleResponseDTO.ScheduleDTO modSchedule(Long studyId, Long scheduleId, ScheduleRequestDTO.ScheduleDTO scheduleModDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 로그인한 회원이 일정 생성자인지 확인
        scheduleRepository.findByIdAndMemberId(scheduleId, memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._SCHEDULE_MOD_INVALID));

        // 해당 스터디의 일정인지 확인
        scheduleRepository.findByIdAndStudyId(scheduleId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND));

        //=== Feature ===//
        schedule.modSchedule(scheduleModDTO);
        schedule = scheduleRepository.save(schedule);

        study.updateSchedule(schedule);
        member.updateSchedule(schedule);

        return ScheduleResponseDTO.ScheduleDTO.toDTO(schedule);
    }

/* ----------------------------- 스터디 출석 관련 API ------------------------------------- */
    // [스터디 출석체크] 출석 퀴즈 생성하기
    @Override
    public StudyQuizResponseDTO.QuizDTO createAttendanceQuiz(Long studyId, StudyQuizRequestDTO.QuizDTO quizRequestDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 로그인한 회원이 스터디장인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, Boolean.TRUE)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_CREATION_INVALID));

        // 이미 출석 퀴즈가 생성되었는지 확인
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Quiz> todayQuizzes = quizRepository.findAllByStudyIdAndCreatedAtBetween(studyId, startOfDay, endOfDay);
        if (!todayQuizzes.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_QUIZ_ALREADY_EXIST);
        }

        //=== Feature ===//
        Quiz quiz = Quiz.builder()
                .study(study)
                .member(member)
                .question(quizRequestDTO.getQuestion())
                .answer(quizRequestDTO.getAnswer())
                .build();

        quiz = quizRepository.save(quiz);
        study.addQuiz(quiz);
        member.addQuiz(quiz);

        return StudyQuizResponseDTO.QuizDTO.toDTO(quiz);
    }

    // [스터디 출석체크] 출석 체크하기
    @Override
    public StudyQuizResponseDTO.AttendanceDTO attendantStudy(Long studyId, Long quizId, StudyQuizRequestDTO.AttendanceDTO attendanceRequestDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), study.getId(), ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 퀴즈인지 확인
        quizRepository.findByIdAndStudyId(quizId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND));

        // 퀴즈 제한시간 확인
        if (LocalDateTime.now().isAfter(quiz.getCreatedAt().plusMinutes(5))) {
            throw new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_VALID);
        }

        // 이미 출석이 완료되었거나 시도 횟수를 초과하였는지 확인
        List<MemberAttendance> attendanceList = memberAttendanceRepository.findByQuizIdAndMemberId(quizId, member.getId());
        int try_num = 0;
        for (MemberAttendance attendance : attendanceList) {
            if (attendance.getIsCorrect())
                throw new StudyHandler(ErrorStatus._STUDY_ATTENDANCE_ALREADY_EXIST);
            else
                try_num++;
        }
        if (try_num >= 3) {
            throw new StudyHandler(ErrorStatus._STUDY_ATTENDANCE_ATTEMPT_LIMIT_EXCEEDED);
        }

        //=== Feature ===//
        Boolean isCorrect;
        if (attendanceRequestDTO.getAnswer().equals(quiz.getAnswer())) {
            isCorrect = Boolean.TRUE;
        } else {
            isCorrect = Boolean.FALSE;
        }

        MemberAttendance memberAttendance = new MemberAttendance(isCorrect);
        member.addMemberAttendance(memberAttendance);
        quiz.addMemberAttendance(memberAttendance);
        memberAttendance = memberAttendanceRepository.save(memberAttendance);

        return StudyQuizResponseDTO.AttendanceDTO.toDTO(memberAttendance, try_num+1);
    }

    // [스터디 출석체크] 출석 퀴즈 삭제하기
    @Override
    public StudyQuizResponseDTO.QuizDTO deleteAttendanceQuiz(Long studyId, Long quizId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), study.getId(), ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 로그인한 회원이 스터디장인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, Boolean.TRUE)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_CREATION_INVALID));

        // 해당 스터디의 퀴즈인지 확인
        quizRepository.findByIdAndStudyId(quizId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND));

        //=== Feature ===//
        memberAttendanceRepository.findByQuizId(quizId)
                .forEach(memberAttendance -> {
                    quiz.deleteMemberAttendance(memberAttendance);
                    memberAttendanceRepository.delete(memberAttendance);
                });
        quizRepository.delete(quiz);

        return StudyQuizResponseDTO.QuizDTO.toDTO(quiz);
    }


/* ----------------------------- 스터디 투표 관련 API ------------------------------------- */

    @Override
    public StudyVoteResponseDTO.VotePreviewDTO createVote(Long studyId, StudyVoteRequestDTO.VoteDTO voteDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member loginMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        //=== Feature ===//
        Vote vote = Vote.builder()
                .study(study)
                .member(loginMember)
                .title(voteDTO.getTitle())
                .isMultipleChoice(voteDTO.getIsMultipleChoice())
                .finishedAt(voteDTO.getFinishedAt())
                .build();

        // Vote 저장
        vote = voteRepository.save(vote);
        // Option 저장
        vote = createOption(vote, voteDTO);
        // 연관관계 매핑
        loginMember.addVote(vote);
        study.addVote(vote);

        return StudyVoteResponseDTO.VotePreviewDTO.toDTO(vote);
    }

    private Vote createOption(Vote vote, StudyVoteRequestDTO.VoteDTO voteDTO) {
        voteDTO.getOptions()
                .forEach(stringOption -> {
                    Option option = Option.builder()
                            .vote(vote)
                            .content(stringOption)
                            .build();
                    option = optionRepository.save(option);
                    vote.addOption(option);
                });
        return voteRepository.save(vote);
    }

    @Override
    public StudyVoteResponseDTO.VotedOptionDTO vote(Long studyId, Long voteId, StudyVoteRequestDTO.VotedOptionDTO votedOptionDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member loginMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));
        voteRepository.findByIdAndStudyId(voteId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 중복 선택이 허용되지 않는 투표는 여러 개의 option을 선택할 수 없음
        if (!vote.getIsMultipleChoice() && votedOptionDTO.getOptionIdList().size() > 1) {
            throw new StudyHandler(ErrorStatus._STUDY_VOTE_MULTIPLE_CHOICE_NOT_VALID);
        }

        // 한 번 참여한 투표는 다시 참여할 수 없음
        optionRepository.findAllByVoteId(voteId)
                .forEach(option -> {
                    if (memberVoteRepository.existsByMemberIdAndOptionId(loginMember.getId(), option.getId())) {
                            throw new StudyHandler(ErrorStatus._STUDY_VOTE_RE_PARTICIPATION_INVALID);
                    }
                });

        //=== Feature ===//
        List<MemberVote> memberVotes = votedOptionDTO.getOptionIdList().stream()
                .map(optionId -> {
                    Option votedOption = optionRepository.findById(optionId)
                            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_OPTION_NOT_FOUND));
                    optionRepository.findByIdAndVoteId(optionId, voteId)
                            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_OPTION_NOT_FOUND));

                    MemberVote memberVote = MemberVote.builder()
                            .member(loginMember)
                            .option(votedOption)
                            .build();

                    memberVote = memberVoteRepository.save(memberVote);
                    loginMember.addMemberVote(memberVote);
                    votedOption.addMemberVote(memberVote);

                    return memberVote;
                })
                .toList();

        return StudyVoteResponseDTO.VotedOptionDTO.toDTO(vote, loginMember, memberVotes);
    }

    @Override
    public StudyVoteResponseDTO.VotePreviewDTO updateVote(Long studyId, Long voteId, StudyVoteRequestDTO.VoteUpdateDTO voteDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member loginMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 로그인한 회원이 투표 생성자인지 확인
        if (!loginMember.equals(vote.getMember())) {
            throw new StudyHandler(ErrorStatus._STUDY_VOTE_CREATOR_NOT_AUTHORIZED);
        }

        // 한 명이라도 투표에 참여했으면 투표 편집 불가
        optionRepository.findAllByVoteId(voteId)
                .forEach(option -> {
                    if (memberVoteRepository.existsByOptionId(option.getId())) {
                        throw new StudyHandler(ErrorStatus._STUDY_VOTE_IS_IN_PROGRESS);
                    }
                });

        //=== Feature ===//
        for (StudyVoteRequestDTO.OptionDTO optionDTO : voteDTO.getOptions()) {
            Option option = optionRepository.findById(optionDTO.getOptionId())
                    .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_OPTION_NOT_FOUND));
            option.setContent(optionDTO.getContent());
            option = optionRepository.save(option);
            vote.updateOption(option);
        }

        vote.updateVote(voteDTO.getTitle(), voteDTO.getIsMultipleChoice(), voteDTO.getFinishedAt());
        vote = voteRepository.save(vote);
        loginMember.updateVote(vote);
        study.updateVote(vote);

        return StudyVoteResponseDTO.VotePreviewDTO.toDTO(vote);
    }

    @Override
    public StudyVoteResponseDTO.VotePreviewDTO deleteVote(Long studyId, Long voteId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member loginMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));
        voteRepository.findByIdAndStudyId(voteId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 로그인한 회원이 투표 생성자인지 확인
        if (!loginMember.equals(vote.getMember())) {
            throw new StudyHandler(ErrorStatus._STUDY_VOTE_CREATOR_NOT_AUTHORIZED);
        }

        //=== Feature ===//
        deleteOptions(voteId);
        loginMember.deleteVote(vote);
        study.deleteVote(vote);
        voteRepository.delete(vote);

        return StudyVoteResponseDTO.VotePreviewDTO.toDTO(vote);
    }

    private void deleteOptions(Long voteId) {
        List<Option> options = optionRepository.findAllByVoteId(voteId);
        options.forEach(option -> {
            option.deleteAllMemberVotes();
            memberVoteRepository.deleteAll(memberVoteRepository.findAllByOptionId(option.getId()));
            optionRepository.delete(option);
        });
    }

    /**
     * 회원이 스터디 장인지 확인합니다.
     * @param memberId 확인 하려는 회원 ID
     * @param studyId 확인 하려는 스터디 ID
     * @return 스터디 장 여부를 반환합니다.
     */
    private boolean isOwner(Long memberId, Long studyId) {
        return memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, true).isPresent();
    }

    /**
     * 회원이 스터디 구성원인지 확인합니다.
     * @param memberId 확인 하려는 회원 ID
     * @param studyId 확인 하려는 스터디 ID
     * @return 스터디 참여 여부를 반환합니다.
     */
    private boolean isMember(Long memberId, Long studyId) {
        return memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED).isPresent();
    }

    @Override
    public MemberResponseDTO.ReportedMemberDTO reportStudyMember(Long studyId, Long memberId, StudyMemberReportDTO studyMemberReportDTO) {

        //=== Exception ===//
        Long reporterId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(reporterId);

        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(reporterId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 신고당한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 자기 자신을 신고할 수 없음
        if (reporterId.equals(memberId)) {
            throw new StudyHandler(ErrorStatus._STUDY_MEMBER_REPORT_INVALID);
        }


        //=== Feature ===//
        MemberReport memberReport = MemberReport.builder()
                .content(studyMemberReportDTO.getContent())
                .member(member)
                .build();

        memberReport = memberReportRepository.save(memberReport);
        member.addMemberReport(memberReport);

        return MemberResponseDTO.ReportedMemberDTO.toDTO(member);
    }

    @Override
    public StudyPostResDTO.PostPreviewDTO reportStudyPost(Long studyId, Long postId) {

        //=== Exception ===//
        Long reporterId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(reporterId);

        memberRepository.findById(reporterId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        StudyPost studyPost = studyPostRepository.findById(postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(reporterId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        //=== Feature ===//
        StudyPostReport studyPostReport = StudyPostReport.builder()
                .studyPost(studyPost)
                .build();

        studyPostReport = studyPostReportRepository.save(studyPostReport);
        studyPost.addStudyPostReport(studyPostReport);

        return StudyPostResDTO.PostPreviewDTO.toDTO(studyPost);
    }

   // * ----------------------------- 스터디 To-Do List 관련 API ------------------------------------- */

    /**
     * To-Do List를 생성합니다.
     * @param studyId 생성할 To-Do List가 속한 스터디 ID
     * @param toDoListCreateDTO 생성할 To-Do List 정보
     * @return 생성된 To-Do List 정보
     * @throws StudyHandler 스터디를 찾을 수 없을 때
     * @throws StudyHandler To-Do List를 생성하는 회원이 스터디 회원이 아닐 때
     * @throws StudyHandler 해당 회원을 찾을 수 없을 때
     */
    @Override
    public ToDoListCreateResponseDTO createToDoList(Long studyId,
        ToDoListCreateDTO toDoListCreateDTO) {

        // 스터디 조회
        Study study = studyRepository.findById(studyId)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // To-Do List를 생성하는 회원 ID 조회
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // To-Do List를 생성하는 회원이 스터디 회원인지 확인
        if (!isMember(currentUserId, studyId))
            throw new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND);

        // 회원 조회
        Member member = memberRepository.findById(currentUserId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // To-Do List 생성
        ToDoList toDoList = ToDoList.builder()
            .study(study)
            .member(member)
            .date(toDoListCreateDTO.getDate())
            .isDone(false)
            .content(toDoListCreateDTO.getContent())
            .build();

        // To-Do List 저장
        toDoList.setToDoList();
        toDoListRepository.save(toDoList);

        // To-Do List 생성 DTO 반환
        return ToDoListCreateResponseDTO.builder()
            .id(toDoList.getId())
            .content(toDoList.getContent())
            .createdAt(toDoList.getCreatedAt())
            .build();
    }

    // studyId가 필요할까?

    /**
     * To-Do List에 작성한 할 일의 체크 상태를 변경 합니다. 체크 상태를 변경 하면 해당 스터디에 참여하고 있는 모든 회원에게 알림이 전송됩니다.
     * @param studyId 스터디 ID
     * @param toDoListId 변경할 To-Do List ID
     * @return To-Do List 변경 여부와 변경 시간
     * @throws StudyHandler To-Do List를 찾을 수 없을 때
     * @throws StudyHandler To-Do List가 스터디에 속하지 않을 때
     * @throws StudyHandler To-Do List를 변경하는 회원이 스터디 회원이 아닐 때
     * @throws StudyHandler 알림 생성 할 스터디 회원을 찾을 수 없을 때
     */
    @Override
    public ToDoListUpdateResponseDTO checkToDoList(Long studyId, Long toDoListId) {

        // To-Do List 조회
        ToDoList toDoList = toDoListRepository.findById(toDoListId)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_TODO_NOT_FOUND));

        // To-Do List가 속한 스터디가 아니면 예외 처리
        if (!Objects.equals(toDoList.getStudy().getId(), studyId))
            throw new StudyHandler(ErrorStatus._STUDY_TODO_IS_NOT_BELONG_TO_STUDY);

        // To-Do List를 변경하는 회원이 스터디 회원이 아니면 예외 처리
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!toDoList.getMember().getId().equals(currentUserId))
            throw new StudyHandler(ErrorStatus._STUDY_TODO_NOT_AUTHORIZED);

        // To-Do List 체크 상태 변경
        toDoList.check();

        // 스터디 회원의 To-Do List 중 하나가 완료 되면, 해당 스터디의 모든 회원에게 알림 전송
        if (toDoList.isDone()){
            List<Member> members = memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED).stream()
                .map(MemberStudy::getMember)
                .toList();

            // 알림을 생성할 회원이 없으면 알림 생성하지 않음
            if (members.isEmpty()){
                return ToDoListUpdateResponseDTO.builder()
                    .id(toDoList.getId())
                    .isDone(toDoList.isDone())
                    .updatedAt(toDoList.getUpdatedAt())
                    .build();
            }

            // 알림 생성
            members.forEach(studyMember -> {
                Notification notification = Notification.builder()
                    .member(studyMember)
                    .notifierName(toDoList.getMember().getName()) // To-Do 완료한 회원 이름
                    .study(toDoList.getStudy())
                    .type(NotifyType.TO_DO_UPDATE)
                    .isChecked(Boolean.FALSE)
                    .build();
                notificationRepository.save(notification);
            });
        }

        // To-Do List 저장
        toDoListRepository.save(toDoList);

        // To-Do List 변경 DTO 반환
        return ToDoListUpdateResponseDTO.builder()
            .id(toDoList.getId())
            .isDone(toDoList.isDone())
            .updatedAt(toDoList.getUpdatedAt())
            .build();
    }


    /**
     * To-Do List 내용을 수정합니다.
     * @param studyId 수정할 To-Do List가 속한 스터디 ID
     * @param toDoListId 수정할 To-Do List ID
     * @param toDoListCreateDTO 수정할 To-Do List 정보
     * @return 수정된 To-Do List 정보
     * @throws StudyHandler To-Do List를 찾을 수 없을 때
     * @throws StudyHandler To-Do List가 스터디에 속하지 않을 때
     * @throws StudyHandler To-Do List를 수정하는 회원이 스터디 회원이 아닐 때
     */
    @Override
    public ToDoListUpdateResponseDTO updateToDoList(Long studyId, Long toDoListId,
        ToDoListCreateDTO toDoListCreateDTO) {

        // To-Do List 조회
        ToDoList toDoList = toDoListRepository.findById(toDoListId)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_TODO_NOT_FOUND));

        // To-Do List가 속한 스터디가 아니면 예외 처리
        if (!Objects.equals(toDoList.getStudy().getId(), studyId))
            throw new StudyHandler(ErrorStatus._STUDY_TODO_IS_NOT_BELONG_TO_STUDY);

        // To-Do List를 수정하는 회원이 스터디 회원이 아니면 예외 처리
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!toDoList.getMember().getId().equals(currentUserId))
            throw new StudyHandler(ErrorStatus._STUDY_TODO_NOT_AUTHORIZED);

        // To-Do List 수정
        toDoList.update(toDoListCreateDTO.getContent(), toDoListCreateDTO.getDate());

        // To-Do List 저장
        toDoListRepository.save(toDoList);

        // To-Do List 변경 DTO 반환
        return ToDoListUpdateResponseDTO.builder()
            .id(toDoList.getId())
            .isDone(toDoList.isDone())
            .updatedAt(toDoList.getUpdatedAt())
            .build();
    }

    /**
     * To-Do List를 삭제합니다.
     * @param studyId 삭제할 To-Do List가 속한 스터디 ID
     * @param toDoListId 삭제할 To-Do List ID
     * @return 삭제된 To-Do List 정보
     * @throws StudyHandler To-Do List를 찾을 수 없을 때
     * @throws StudyHandler To-Do List가 스터디에 속하지 않을 때
     * @throws StudyHandler To-Do List를 삭제하는 회원이 스터디 회원이 아닐 때
     */
    @Override
    public ToDoListUpdateResponseDTO deleteToDoList(Long studyId, Long toDoListId) {

        // 로그인 중인 회원 ID 조회
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // To-Do List를 삭제하는 회원이 스터디 회원인지 확인
        if (!isMember(currentUserId, studyId))
            throw new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND);

        // To-Do List 조회
        ToDoList toDoList = toDoListRepository.findById(toDoListId)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_TODO_NOT_FOUND));

        // To-Do List가 속한 스터디가 아니면 예외 처리
        if (!Objects.equals(toDoList.getStudy().getId(), studyId))
            throw new StudyHandler(ErrorStatus._STUDY_TODO_IS_NOT_BELONG_TO_STUDY);

        // To-Do List를 삭제하는 회원의 ID와 To-Do List를 생성한 회원의 ID가 다르면 예외 처리
        if (!toDoList.getMember().getId().equals(currentUserId))
            throw new StudyHandler(ErrorStatus._STUDY_TODO_NOT_AUTHORIZED);

        // To-Do List 삭제
        toDoListRepository.deleteById(toDoListId);

        // To-Do List 삭제 DTO 반환
        return ToDoListUpdateResponseDTO.builder()
            .id(toDoList.getId())
            .isDone(toDoList.isDone())
            .updatedAt(toDoList.getUpdatedAt())
            .build();
    }

}

package com.example.spot.service.memberstudy;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Quiz;
import com.example.spot.domain.enums.Period;
import com.example.spot.domain.mapping.MemberAttendance;
import com.example.spot.domain.study.*;
import com.example.spot.repository.*;

import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListSearchResponseDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListSearchResponseDTO.ToDoListDTO;
import com.example.spot.web.dto.memberstudy.response.*;

import com.example.spot.web.dto.study.response.*;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO.StudyApplicantDTO;
import lombok.RequiredArgsConstructor;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO.StudyApplyMemberDTO;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO.StudyMemberDTO;
import com.example.spot.web.dto.study.response.StudyScheduleResponseDTO.StudyScheduleDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberStudyQueryServiceImpl implements MemberStudyQueryService {

    @Value("${image.post.anonymous.profile}")
    private String defaultImage;

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final StudyPostRepository studyPostRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final MemberAttendanceRepository memberAttendanceRepository;
    private final QuizRepository quizRepository;
    private final VoteRepository voteRepository;
    private final OptionRepository optionRepository;
    private final MemberVoteRepository memberVoteRepository;
    private final ToDoListRepository toDoListRepository;


    /**
     * 스터디 최근 공지사항을 1개 조회합니다.
     * @param studyId 스터디 ID
     * @return 제목과 내용을 반환합니다.
     * @throws GeneralException 스터디 공지사항이 존재하지 않는 경우
     * @throws GeneralException 스터디 멤버가 아닌 경우
     */
    @Override
    public StudyPostResponseDTO findStudyAnnouncementPost(Long studyId) {

        // 로그인한 회원이 해당 스터디 회원인지 확인
        if (!isMember(SecurityUtils.getCurrentUserId(), studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_MEMBER_CAN_ACCESS_ANNOUNCEMENT_POST);

        // 스터디 공지사항 조회
        StudyPost studyPost = studyPostRepository.findByStudyIdAndIsAnnouncement(
            studyId, true).orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_POST_NOT_FOUND));

        // DTO로 변환하여 반환
        return StudyPostResponseDTO.builder()
            .title(studyPost.getTitle())
            .content(studyPost.getContent()).build();
    }

    /**
     * 로그인한 회원이 참여하는 특정 스터디의 다가오는 모임 목록을 페이징 조회 합니다.
     * @param studyId 스터디 ID
     * @param pageable 페이징 정보
     * @return 다가오는 모임 목록을 반환합니다.
     * @throws GeneralException 스터디 일정이 존재하지 않는 경우
     * @throws GeneralException 스터디 멤버가 아닌 경우
     */
    @Override
    public StudyScheduleResponseDTO findStudySchedule(Long studyId, Pageable pageable) {

        // 로그인한 회원이 해당 스터디 회원인지 확인
        if (!isMember(SecurityUtils.getCurrentUserId(), studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_MEMBER_CAN_ACCESS_SCHEDULE);

        // 스터디 일정 조회
        List<Schedule> schedules = scheduleRepository.findAllByStudyId(studyId, pageable);

        // 스터디 일정이 존재하지 않는 경우
        if (schedules.isEmpty())
            throw  new GeneralException(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND);

        // DTO로 변환하여 반환
        List<StudyScheduleDTO> scheduleDTOS = schedules.stream().map(schedule -> StudyScheduleDTO.builder()
            .title(schedule.getTitle())
            .location(schedule.getLocation())
            .staredAt(schedule.getStartedAt())
            .build()).toList();

        // 페이징 처리
        return new StudyScheduleResponseDTO(new PageImpl<>(scheduleDTOS, pageable, schedules.size()), scheduleDTOS, schedules.size());
    }

    /**
     * 특정 스터디의 회원 목록을 전체 조회 합니다. 가입된 스터디가 아니더라도 회원 목록을 조회할 수 있습니다.
     * @param studyId 스터디 ID
     * @return 스터디에 참여하는 회원 목록을 반환합니다.
     * @throws GeneralException 스터디 할 일이 존재하지 않는 경우
     * @throws GeneralException 스터디 멤버가 아닌 경우
     */
    @Override
    public StudyMemberResponseDTO findStudyMembers(Long studyId) {
//       // 스터디에 가입하지 않은 사람도 회원 목록은 볼 수 있어야 함.
//        if (!isMember(SecurityUtils.getCurrentUserId(), studyId))
//            throw new GeneralException(ErrorStatus._ONLY_STUDY_MEMBER_CAN_ACCESS_MEMBERS);

        // 스터디 멤버 조회
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED);

        // 스터디 멤버가 존재하지 않는 경우
        if (memberStudies.isEmpty())
            throw new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND);

        // DTO로 변환하여 반환
        List<StudyMemberDTO> memberDTOS = memberStudies.stream().map(memberStudy -> StudyMemberDTO.builder()
            .memberId(memberStudy.getMember().getId())
            .nickname(memberStudy.getMember().getName())
            .profileImage(memberStudy.getMember().getProfileImage())
            .build()).toList();
        // DTO로 변환하여 반환
        return new StudyMemberResponseDTO(memberDTOS);
    }


    /**
     * 회원이 모집중인 스터디에 신청한 회원 목록을 불러옵니다.
     * @param studyId 스터디 ID
     * @return 스터디 신청자 목록을 반환합니다.
     * @throws GeneralException 스터디 신청자가 존재 하지 않는 경우
     * @throws GeneralException 조회 하는 회원이 스터디 장이 아닌 경우
     */
    @Override
    public StudyMemberResponseDTO findStudyApplicants(Long studyId) {

        // 로그인한 회원이 해당 스터디 장인지 확인
        if (!isOwner(SecurityUtils.getCurrentUserId(), studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_OWNER_CAN_ACCESS_APPLICANTS);

        // 스터디 신청자 조회
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPLIED);

        // 스터디 신청자가 존재하지 않는 경우
        if (memberStudies.isEmpty())
            throw new GeneralException(ErrorStatus._STUDY_APPLICANT_NOT_FOUND);

        // DTO로 변환하여 반환
        List<StudyMemberDTO> memberDTOS = memberStudies.stream().map(memberStudy -> StudyMemberDTO.builder()
            .memberId(memberStudy.getMember().getId())
            .nickname(memberStudy.getMember().getName())
            .profileImage(memberStudy.getMember().getProfileImage())
            .build()).toList();

        // DTO로 변환하여 반환
        return new StudyMemberResponseDTO(memberDTOS);
    }

    /**
     * 스터디 신청자의 정보를 조회합니다.
     * @param studyId 스터디 ID
     * @param memberId 회원 ID
     * @return 스터디 신청자 정보를 반환합니다.
     * @throws GeneralException 스터디 신청자가 존재하지 않는 경우
     * @throws GeneralException 조회 하는 회원이 스터디 장이 아닌 경우
     * @throws GeneralException 스터디 장은 스터디에 신청할 수 없음
     */
    @Override
    public StudyApplyMemberDTO findStudyApplication(Long studyId, Long memberId) {

        // 로그인한 회원이 해당 스터디 장인지 확인
        if (!isOwner(SecurityUtils.getCurrentUserId(), studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_OWNER_CAN_ACCESS_APPLICANTS);

        // 스터디 신청자 조회
        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPLIED)
            .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_APPLICANT_NOT_FOUND));

        // 스터디 장은 스터디에 신청할 수 없음
        if (memberStudy.getIsOwned())
            throw new GeneralException(ErrorStatus._STUDY_OWNER_CANNOT_APPLY);

        // DTO로 변환하여 반환
        return StudyApplyMemberDTO.builder()
            .memberId(memberStudy.getMember().getId())
            .studyId(memberStudy.getStudy().getId())
            .introduction(memberStudy.getIntroduction())
            .nickname(memberStudy.getMember().getName())
            .profileImage(memberStudy.getMember().getProfileImage())
            .build();
    }

    /**
     * 해당 스터디 신청 여부를 조회합니다. true: 신청, false: 미신청
     * @param studyId 스터디 ID
     * @return 신청 여부와 스터디 ID를 반환합니다.
     * @throws GeneralException 이미 스터디 멤버인 경우
     */
    @Override
    public StudyApplicantDTO isApplied(Long studyId) {
        // 로그인한 회원 ID 조회
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 이미 스터디 멤버인 경우
        if (isMember(currentUserId, studyId))
            throw new GeneralException(ErrorStatus._ALREADY_STUDY_MEMBER);

        // DTO로 변환하여 반환
        return StudyApplicantDTO.builder()
            .isApplied(memberStudyRepository.existsByMemberIdAndStudyIdAndStatus(currentUserId, studyId, ApplicationStatus.APPLIED))
            .studyId(studyId)
            .build();

    }

    /* ----------------------------- 스터디 출석 관련 API ------------------------------------- */

    /**
     * 금일 모든 스터디 회원의 출석 정보를 불러옵니다.
     *
     * @param studyId    출석 정보를 불러올 스터디의 아이디를 입력 받습니다.
     * @param scheduleId 스터디 일정의 아이디를 입력 받습니다.
     * @param date
     * @return 모든 스터디 회원에 대한 정보와 출석 여부를 담은 리스트를 반환합니다.
     */
    @Override
    public StudyQuizResponseDTO.AttendanceListDTO getAllAttendances(Long studyId, Long scheduleId, LocalDate date) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 요청한 날짜에 생성된 출석 퀴즈 조회
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atStartOfDay().plusDays(1);
        List<Quiz> todayQuizzes = quizRepository.findAllByScheduleIdAndCreatedAtBetween(scheduleId, startOfDay, endOfDay);
        if (todayQuizzes.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND);
        }
        Quiz quiz = todayQuizzes.get(0);

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        //=== Feature ===//
        List<StudyQuizResponseDTO.StudyMemberDTO> studyMembers = memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED).stream()
                .map(memberStudy -> {
                    List<MemberAttendance> attendanceList = memberAttendanceRepository.findByQuizIdAndMemberId(quiz.getId(), memberStudy.getMember().getId());
                    for (MemberAttendance attendance : attendanceList) {
                        // MemberAttendance에 퀴즈에 대한 정답이 저장되어 있으면 금일 출석 성공
                        if (attendance.getIsCorrect())
                            return StudyQuizResponseDTO.StudyMemberDTO.toDTO(memberStudy, Boolean.TRUE);
                    }
                    // 퀴즈를 풀지 않았거나 MemberAttendance에 오답만 저장되어 있으면 금일 출석 실패
                    return StudyQuizResponseDTO.StudyMemberDTO.toDTO(memberStudy, Boolean.FALSE);
                })
                .toList();

        return StudyQuizResponseDTO.AttendanceListDTO.toDTO(quiz, studyMembers);

    }

    @Override
    public StudyQuizResponseDTO.QuizDTO getAttendanceQuiz(Long studyId, Long scheduleId, LocalDate date) {

        // Authorization
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND));

        // 해당 스터디에서 생성된 일정인지 확인
        if (!schedule.getStudy().equals(study)) {
            throw new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND);
        }

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 날짜에 생성된 스터디 퀴즈 조회
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atStartOfDay().plusDays(1);
        List<Quiz> todayQuizzes = quizRepository.findAllByScheduleIdAndCreatedAtBetween(scheduleId, startOfDay, endOfDay);
        if (todayQuizzes.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND);
        }
        Quiz quiz = todayQuizzes.get(0);

        return StudyQuizResponseDTO.QuizDTO.toDTO(quiz);
    }

    /* ----------------------------- 스터디 일정 관련 API ------------------------------------- */

    /**
     * 특정 연/월의 일정을 불러오는 메서드입니다.
     * @param studyId 일정을 불러올 스터디의 아이디를 입력 받습니다.
     * @param year 일정을 불러올 기준 연도를 입력 받습니다.
     * @param month 일정을 불러올 달을 입력 받습니다.
     * @return 스터디 아이디와 해당 스터디의 월별 일정 목록을 반환합니다.
     */
    @Override
    public ScheduleResponseDTO.MonthlyScheduleListDTO getMonthlySchedules(Long studyId, int year, int month) {

        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        boolean isStudyMember;
        if (memberStudyRepository.existsByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)) {
            isStudyMember = true;
        } else {
            isStudyMember = false;
        }

        List<ScheduleResponseDTO.MonthlyScheduleDTO> monthlyScheduleDTOS = new ArrayList<>();

        study.getSchedules().forEach(schedule -> {
                    if (schedule.getPeriod().equals(Period.NONE)) {
                        addSchedule(schedule, year, month, monthlyScheduleDTOS, isStudyMember);
                    } else {
                        addPeriodSchedules(schedule, year, month, monthlyScheduleDTOS, isStudyMember);
                    }
                });

        return ScheduleResponseDTO.MonthlyScheduleListDTO.toDTO(study, monthlyScheduleDTOS);
    }

    /**
     * 하나의 일정에 대한 상세 정보를 불러오는 메서드입니다.
     * @param studyId 일정을 불러올 스터디의 아이디를 입력 받습니다.
     * @param scheduleId 상세 정보를 물러올 일정의 아이디를 입력 받습니다.
     * @return 일정 아이디, 제목, 위치, 시작 일시, 종료 일시, 매일 진행 여부, 주기를 반환합니다.
     */
    @Override
    public ScheduleResponseDTO.MonthlyScheduleDTO getSchedule(Long studyId, Long scheduleId) {

        // Exception
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        boolean isStudyMember;
        if (memberStudyRepository.existsByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)) {
            isStudyMember = true;
        } else {
            isStudyMember = false;
        }

        // 해당 스터디의 일정인지 확인
        scheduleRepository.findByIdAndStudyId(scheduleId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND));

        return ScheduleResponseDTO.MonthlyScheduleDTO.toDTO(schedule, isStudyMember);
    }

    /**
     * 월별 일정 리스트에 주기가 정해져 있지 않은 일정을 추가하기 위한 메서드입니다.
     * 일정의 시작일이 기준 연월과 일치하는 경우 월별 일정 리스트에 추가합니다.
     * getMonthlySchedules API에서 호출되는 내부 메서드입니다.
     * @param schedule 리스트에 추가할 일정 정보를 입력 받습니다.
     * @param year 기준 연도를 입력 받습니다.
     * @param month 기준 월을 입력 받습니다.
     * @param monthlyScheduleDTOS 일정을 추가할 월별 일정 리스트를 입력 받습니다.
     * @param isStudyMember 스터디 회원 여부를 입력 받습니다.
     */
    private void addSchedule(Schedule schedule, int year, int month, List<ScheduleResponseDTO.MonthlyScheduleDTO> monthlyScheduleDTOS, boolean isStudyMember) {
        if (schedule.getStartedAt().getYear() == year && schedule.getStartedAt().getMonthValue() == month) {
            monthlyScheduleDTOS.add(ScheduleResponseDTO.MonthlyScheduleDTO.toDTO(schedule, isStudyMember));
        }
    }

    /**
     * 월별 일정 리스트에 반복적인 일정을 추가하기 위한 메서드입니다.
     * 일정의 시작일이 기준 연월 내에 있는 경우에만 일정을 추가하며, 주기에 따라 하나의 일정이라도 여러 번 추가될 수 있습니다.
     * 예를 들어 기준 연월이 2024년 8월이고, 2024년 8월 2일부터 시작되는 WEEKLY 일정이 있다고 가정
     *      1. 이 일정은 기준 연월 내에서 2024년 8월 2일, 8월 9일, 8월 16일, 8월 23일, 8월 30일에 시행
     *      2. 따라서 monthlyScheduleDTOS에 추가되는 일정은 총 5개
     * @param schedule 리스트에 추가할 일정 정보를 입력 받습니다.
     * @param year 기준 연도를 입력 받습니다.
     * @param month 기준 월을 입력 받습니다.
     * @param monthlyScheduleDTOS 일정을 추가할 월별 일정 리스트를 입력 받습니다.
     * @param isStudyMember 스터디 회원 여부를 입력 받습니다.
     */
    private void addPeriodSchedules(Schedule schedule, int year, int month, List<ScheduleResponseDTO.MonthlyScheduleDTO> monthlyScheduleDTOS, boolean isStudyMember) {

        LocalDateTime startedAt = schedule.getStartedAt();
        LocalDateTime finishedAt = schedule.getFinishedAt();

        YearMonth yearMonth = YearMonth.of(year, month); // 탐색 연월
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59); // 탐색 연월의 마지막 날

        // 일정 시작일이 탐색 연월 내에 있는 경우만 반복
        while (startedAt.isBefore(endOfMonth)) {
            // 업데이트된 일정 시작일의 month가 탐색 month와 일치하면 추가
            if (startedAt.getMonthValue() == month) {
                monthlyScheduleDTOS.add(ScheduleResponseDTO.MonthlyScheduleDTO.toDTOWithDate(schedule, startedAt, finishedAt, isStudyMember));
            }

            if (schedule.getPeriod().equals(Period.DAILY)) {
                startedAt = startedAt.plusDays(1);
                finishedAt = finishedAt.plusDays(1);
            } else if (schedule.getPeriod().equals(Period.WEEKLY)) {
                startedAt = startedAt.plusWeeks(1);
                finishedAt = finishedAt.plusWeeks(1);
            } else if (schedule.getPeriod().equals(Period.BIWEEKLY)) {
                startedAt = startedAt.plusWeeks(2);
                finishedAt = finishedAt.plusWeeks(2);
            } else if (schedule.getPeriod().equals(Period.MONTHLY)) {
                startedAt = startedAt.plusMonths(1);
                finishedAt = finishedAt.plusMonths(1);
            }
        }
    }

/* ----------------------------- 스터디 투표 관련 API ------------------------------------- */

    /**
     * 스터디에 생성된 모든 투표 목록을 불러옵니다.
     * @param studyId 투표 목록을 불러올 타겟 스터디의 아이디를 입력 받습니다.
     * @return 스터디 아이디와 해당 스터디에서 진행중인 투표 목록, 마감된 투표 목록을 반환합니다.
     */
    @Override
    public StudyVoteResponseDTO.VoteListDTO getAllVotes(Long studyId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        //=== Feature ===//

        // 진행중인 투표 목록
        List<StudyVoteResponseDTO.VoteInfoDTO> votesInProgress = voteRepository.findAllByStudyIdAndFinishedAtAfter(studyId, LocalDateTime.now()).stream()
                .map(vote -> {
                    boolean isParticipated = isParticipated(vote, member);
                    return StudyVoteResponseDTO.VoteInfoDTO.toDTO(vote, isParticipated);
                })
                .toList();

        // 마감된 투표 목록
        List<StudyVoteResponseDTO.VoteInfoDTO> votesInCompletion = voteRepository.findAllByStudyIdAndFinishedAtBefore(studyId, LocalDateTime.now()).stream()
                .map(vote -> {
                    boolean isParticipated = isParticipated(vote, member);
                    return StudyVoteResponseDTO.VoteInfoDTO.toDTO(vote, isParticipated);
                })
                .toList();

        return StudyVoteResponseDTO.VoteListDTO.toDTO(studyId, votesInProgress, votesInCompletion);
    }

    /**
     * 스터디 회원의 투표 참여 여부를 확인하는 메서드입니다.
     * getAllVotes에서 사용되는 내부 메서드입니다.
     * @param vote 스터디에서 생성한 투표의 아이디를 입력 받습니다.
     * @param loginMember 로그인한 회원의 정보를 입력 받습니다.
     * @return 투표 참여 여부를 true or false로 반환합니다.
     */
    private boolean isParticipated(Vote vote, Member loginMember) {
        // 투표 참여 여부 확인
        boolean isParticipated = false;
        for (Option option : vote.getOptions()) {
            if (memberVoteRepository.existsByMemberIdAndOptionId(loginMember.getId(), option.getId())) {
                isParticipated = true;
            }
        }
        return isParticipated;
    }

    /**
     * 입력 받은 스터디 투표가 종료되었는지 확인하는 메서드입니다.
     * (클라이언트에서 투표 불러오기 API를 호출할 때 스터디 종료 여부에 따라 Response DTO가 바뀌어야 하기 때문에 필요한 메서드입니다)
     * @param voteId 스터디에서 생성한 투표의 아이디를 입력 받습니다.
     * @return 투표 종료 여부를 true or false로 반환합니다.
     */
    @Override
    public Boolean getIsCompleted(Long voteId) {
        return voteRepository.existsByIdAndFinishedAtBefore(voteId, LocalDateTime.now());
    }

    /**
     * 종료된 투표의 정보를 불러오는 메서드입니다.
     * @param studyId 타겟 스터디의 아이디를 입력 받습니다.
     * @param voteId 스터디에서 생성한 투표의 아이디를 입력 받습니다.
     * @return 종료된 투표의 아이디, 생성자, 제목, 항목별 투표 인원수, 전체 참여자 수, 종료 일시를 반환합니다.
     */
    @Override
    public StudyVoteResponseDTO.CompletedVoteDTO getVoteInCompletion(Long studyId, Long voteId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 투표인지 확인
        voteRepository.findByIdAndStudyId(voteId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        //=== Feature ===//
        return StudyVoteResponseDTO.CompletedVoteDTO.toDTO(vote);

    }

    /**
     * 진행중인 투표의 정보를 불러오는 메서드입니다.
     * @param studyId 타겟 스터디의 아이디를 입력 받습니다.
     * @param voteId 스터디에서 생성한 투표의 아이디를 입력 받습니다.
     * @return 진행중인 투표의 아이디, 생성자, 제목, 항목 리스트, 복수 선택 가능 여부, 종료 일시, 로그인한 회원의 참여 여부를 반환합니다.
     */
    @Override
    public StudyVoteResponseDTO.VoteDTO getVoteInProgress(Long studyId, Long voteId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 해당 스터디의 투표인지 확인
        voteRepository.findByIdAndStudyId(voteId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        //=== Feature ===//
        return StudyVoteResponseDTO.VoteDTO.toDTO(vote, member);
    }

    /**
     * 마감된 투표에 대해 항목별 투표 현황을 불러오는 메서드입니다.
     * @param studyId 타겟 스터디의 아이디를 입력 받습니다.
     * @param voteId 마감된 스터디 투표의 아이디를 입력 받습니다.
     * @return 마감된 투표의 아이디와 제목, 항목별 투표 회원 목록을 반환합니다.
     */
    @Override
    public StudyVoteResponseDTO.CompletedVoteDetailDTO getCompletedVoteDetail(Long studyId, Long voteId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 해당 스터디의 투표인지 확인
        voteRepository.findByIdAndStudyId(voteId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 마감된 투표인지 확인
        if (!voteRepository.existsByIdAndFinishedAtBefore(voteId, LocalDateTime.now())) {
            throw new StudyHandler(ErrorStatus._STUDY_VOTE_NOT_COMPLETED);
        }

        //=== Feature ===//
        return StudyVoteResponseDTO.CompletedVoteDetailDTO.toDTO(vote);
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
/* ----------------------------- 스터디 갤러리 관련 API ------------------------------------- */

    /**
     * 스터디 게시판에 업로드한 이미지 목록을 불러오는 메서드입니다.
     * @param studyId 타겟 스터디의 아이디를 입력 받습니다.
     * @param pageRequest 페이징에 필요한 페이지 번호와 크기를 입력 받습니다.
     * @return 스터디 아이디와 해당 스터디에 업로드된 이미지 목록을 반환합니다.
     */
    @Override
    public StudyImageResponseDTO.ImageListDTO getAllStudyImages(Long studyId, PageRequest pageRequest) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        memberRepository.findById(memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        //=== Feature ===//
        List<StudyImageResponseDTO.ImageDTO> images = studyPostRepository.findAllByStudyId(studyId, pageRequest)
                .stream()
                .sorted(Comparator.comparing(StudyPost::getCreatedAt).reversed())
                .flatMap(studyPost -> studyPost.getImages().stream())
                .map(StudyImageResponseDTO.ImageDTO::toDTO)
                .toList();

        return StudyImageResponseDTO.ImageListDTO.toDTO(studyId, images);

    }

/* ----------------------------- To-do list 관련 API ------------------------------------- */

    /**
     * 특정 스터디에 저장된 내 To-Do List를 날짜 별로 페이징 조회합니다.
     * @param studyId 스터디 ID
     * @param date 조회하려는 날짜
     * @param pageRequest 페이징 정보
     * @return To-Do List 목록을 반환합니다.
     * @throws GeneralException 스터디 멤버가 아닌 경우
     * @throws GeneralException 스터디 할 일이 존재하지 않는 경우
     */
    @Override
    public ToDoListSearchResponseDTO getToDoList(Long studyId, LocalDate date, PageRequest pageRequest) {
        // 로그인 중인 회원 ID 조회
        Long memberId = SecurityUtils.getCurrentUserId();

        // 로그인한 회원이 스터디 회원인지 확인
        if (!isMember(memberId, studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_MEMBER_CAN_ACCESS_TODO_LIST);

        // 페이징 처리
        List<ToDoList> toDoLists = toDoListRepository.findByStudyIdAndMemberIdAndDateOrderByCreatedAtDesc(
            studyId, memberId, date, pageRequest);

        // 스터디 투 두 리스트가 존재하지 않는 경우
        if (toDoLists.isEmpty())
            throw new GeneralException(ErrorStatus._STUDY_TODO_NOT_FOUND);

        // 투 두 리스트 갯수 조회
        long totalElements = toDoListRepository.countByStudyIdAndMemberIdAndDate(studyId, memberId, date);

        // DTO로 변환
        List<ToDoListDTO> toDoListDTOS = getToDoListDTOS(toDoLists);

        return new ToDoListSearchResponseDTO(
            new PageImpl<>(toDoListDTOS, pageRequest, totalElements), toDoListDTOS, totalElements);
    }

    /**
     * 특정 스터디에 저장된 다른 스터디원의 To-Do List를 날짜 별로 페이징 조회합니다.
     * @param studyId 스터디 ID
     * @param memberId 조회하려는 회원 ID
     * @param date 조회하려는 날짜
     * @param pageRequest 페이징 정보
     * @return To-Do List 목록을 반환합니다.
     * @throws GeneralException 스터디 멤버가 아닌 경우
     * @throws GeneralException 조회하려는 회원이 스터디 멤버가 아닌 경우
     * @throws GeneralException 스터디 할 일이 존재하지 않는 경우
     */
    @Override
    public ToDoListSearchResponseDTO getMemberToDoList(Long studyId, Long memberId, LocalDate date,
        PageRequest pageRequest) {

        // 로그인 중인 회원이 스터디 회원인지 확인
        if (!isMember(SecurityUtils.getCurrentUserId(), studyId))
            throw new GeneralException(ErrorStatus._ONLY_STUDY_MEMBER_CAN_ACCESS_TODO_LIST);

        // 조회하려는 회원이 스터디 회원인지 확인
        if (!isMember(memberId, studyId))
            throw new GeneralException(ErrorStatus._TODO_LIST_MEMBER_NOT_FOUND);

        // 조회하려는 회원의 투 두 리스트 조회
        List<ToDoList> toDoLists = toDoListRepository.findByStudyIdAndMemberIdAndDateOrderByCreatedAtDesc(
            studyId, memberId, date, pageRequest);

        // 투 두 리스트가 존재하지 않는 경우
        if (toDoLists.isEmpty())
            throw new GeneralException(ErrorStatus._STUDY_TODO_NOT_FOUND);

        // 투 두 리스트 갯수 조회
        long totalElements = toDoListRepository.countByStudyIdAndMemberIdAndDate(studyId, memberId, date);

        // DTO로 변환
        List<ToDoListDTO> toDoListDTOS = getToDoListDTOS(toDoLists);

        return new ToDoListSearchResponseDTO(
            new PageImpl<>(toDoListDTOS, pageRequest, totalElements), toDoListDTOS, totalElements);
    }

    /**
     * 투 두 리스트를 DTO로 변환합니다.
     * @param toDoLists 투 두 리스트
     * @return 투 두 리스트 DTO 목록을 반환합니다.
     */
    private static List<ToDoListDTO> getToDoListDTOS(List<ToDoList> toDoLists) {
        List<ToDoListDTO> toDoListDTOS = toDoLists.stream()
            .map(toDoList -> ToDoListDTO.builder()
                .id(toDoList.getId())
                .content(toDoList.getContent())
                .date(toDoList.getDate())
                .isDone(toDoList.isDone())
                .build())
            .toList();
        return toDoListDTOS;
    }

}

package com.example.spot.service.memberstudy;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Quiz;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.mapping.MemberAttendance;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.StudyLikedPost;
import com.example.spot.domain.mapping.StudyPostImage;
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.domain.study.StudyPostComment;
import com.example.spot.repository.*;
import com.example.spot.service.s3.S3ImageService;
import com.example.spot.web.dto.memberstudy.request.StudyQuizRequestDTO;
import com.example.spot.web.dto.memberstudy.response.StudyQuizResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyTerminationResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyWithdrawalResponseDTO;
import com.example.spot.web.dto.study.request.StudyPostRequestDTO;
import com.example.spot.web.dto.study.response.StudyApplyResponseDTO;
import com.example.spot.web.dto.study.request.ScheduleRequestDTO;
import com.example.spot.web.dto.study.response.ScheduleResponseDTO;
import com.example.spot.web.dto.study.response.StudyPostResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class MemberStudyCommandServiceImpl implements MemberStudyCommandService {

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final ScheduleRepository scheduleRepository;
    private final QuizRepository quizRepository;

    private final MemberStudyRepository memberStudyRepository;
    private final MemberAttendanceRepository memberAttendanceRepository;
    private final StudyPostRepository studyPostRepository;
    private final StudyPostImageRepository studyPostImageRepository;
    private final StudyPostCommentRepository studyPostCommentRepository;
    private final StudyLikedPostRepository studyLikedPostRepository;

    // S3 Service
    private final S3ImageService s3ImageService;

/* ----------------------------- 진행중인 스터디 관련 API ------------------------------------- */

    // [진행중인 스터디] 스터디 탈퇴하기
    public StudyWithdrawalResponseDTO.WithdrawalDTO withdrawFromStudy(Long memberId, Long studyId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyId(memberId, studyId)
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

    // [진행중인 스터디] 스터디 끝내기
    public StudyTerminationResponseDTO.TerminationDTO terminateStudy(Long studyId) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        study.setStatus(Status.OFF);
        studyRepository.save(study);

        return StudyTerminationResponseDTO.TerminationDTO.toDTO(study);
    }

    @Override
    public StudyApplyResponseDTO acceptAndRejectStudyApply(Long memberId, Long studyId,
        boolean isAccept) {

        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyId(memberId, studyId)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_APPLICANT_NOT_FOUND));

        if (memberStudy.getIsOwned())
            throw new GeneralException(ErrorStatus._STUDY_OWNER_CANNOT_APPLY);

        if (memberStudy.getStatus() != ApplicationStatus.APPLIED)
            throw new GeneralException(ErrorStatus._STUDY_APPLY_ALREADY_PROCESSED);

        if (isAccept)
            memberStudy.setStatus(ApplicationStatus.APPROVED);
        else {
            memberStudy.setStatus(ApplicationStatus.REJECTED);
            memberStudyRepository.delete(memberStudy);
        }

        return StudyApplyResponseDTO.builder()
            .status(memberStudy.getStatus())
            .updatedAt(memberStudy.getUpdatedAt())
            .build();
    }

/* ----------------------------- 스터디 일정 관련 API ------------------------------------- */
    @Override
    public ScheduleResponseDTO.ScheduleDTO addSchedule(Long studyId, ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO) {

        //=== Exception ===//
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        //=== Feature ===//
        Schedule schedule = Schedule.builder()
                .title(scheduleRequestDTO.getLocation())
                .location(scheduleRequestDTO.getLocation())
                .startedAt(scheduleRequestDTO.getStartedAt())
                .finishedAt(scheduleRequestDTO.getFinishedAt())
                .isAllDay(scheduleRequestDTO.getIsAllDay())
                .period(scheduleRequestDTO.getPeriod())
                .build();

        study.addSchedule(schedule);
        scheduleRepository.save(schedule);

        return ScheduleResponseDTO.ScheduleDTO.toDTO(schedule);
    }

    @Override
    public ScheduleResponseDTO.ScheduleDTO modSchedule(Long studyId, Long scheduleId, ScheduleRequestDTO.ScheduleDTO scheduleModDTO) {

        //=== Exception ===//
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND));

        scheduleRepository.findByIdAndStudyId(scheduleId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_SCHEDULE_NOT_FOUND));

        //=== Feature ===//
        schedule.modSchedule(scheduleModDTO);
        study.updateSchedule(schedule);
        scheduleRepository.save(schedule);

        return ScheduleResponseDTO.ScheduleDTO.toDTO(schedule);
    }

/* ----------------------------- 스터디 출석 관련 API ------------------------------------- */
    // [스터디 출석체크] 출석 퀴즈 생성하기
    @Override
    public StudyQuizResponseDTO.QuizDTO createAttendanceQuiz(Long studyId, StudyQuizRequestDTO.QuizDTO quizRequestDTO) {

        //=== Exception ===//
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 이미 출석 퀴즈가 생성되었는지 확인
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        List<Quiz> todayQuizzes = quizRepository.findAllByStudyIdAndCreatedAtBetween(studyId, startOfDay, endOfDay);
        if (!todayQuizzes.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_QUIZ_ALREADY_EXIST);
        }

        //=== Feature ===//
        Quiz quiz = Quiz.builder()
                .question(quizRequestDTO.getQuestion())
                .answer(quizRequestDTO.getAnswer())
                .build();

        study.addQuiz(quiz);
        quiz = quizRepository.save(quiz);

        return StudyQuizResponseDTO.QuizDTO.toDTO(quiz);
    }

    // [스터디 출석체크] 출석 체크하기
    @Override
    public StudyQuizResponseDTO.AttendanceDTO attendantStudy(Long studyId, Long quizId, StudyQuizRequestDTO.AttendanceDTO attendanceRequestDTO) {

        //=== Exception ===//
        Member member = memberRepository.findById(attendanceRequestDTO.getMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND));
        quizRepository.findByIdAndStudyId(quizId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND));
        memberStudyRepository.findByMemberIdAndStudyId(member.getId(), study.getId())
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

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

        return StudyQuizResponseDTO.AttendanceDTO.toDTO(memberAttendance);
    }

    // [스터디 출석체크] 출석 퀴즈 삭제하기
    @Override
    public StudyQuizResponseDTO.QuizDTO deleteAttendanceQuiz(Long studyId, Long quizId) {

        //=== Exception ===//
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_QUIZ_NOT_FOUND));
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

/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    @Override
    public StudyPostResDTO.PostPreviewDTO createPost(Long studyId, StudyPostRequestDTO.PostDTO postRequestDTO) {

        //=== Exception ===//
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Member member = memberRepository.findById(postRequestDTO.getMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 스터디장만 공지 가능
        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyId(member.getId(), studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
        if (!memberStudy.getIsOwned() && postRequestDTO.getIsAnnouncement()) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_ANNOUNCEMENT_INVALID);
        }

        //=== Feature ===//
        StudyPost studyPost = StudyPost.builder()
                .isAnnouncement(postRequestDTO.getIsAnnouncement())
                .theme(postRequestDTO.getTheme())
                .title(postRequestDTO.getTitle())
                .content(postRequestDTO.getContent())
                .build();

        // 공지면 announcedAt 설정
        if (studyPost.getIsAnnouncement()) {
            studyPost.setAnnouncedAt(LocalDateTime.now());
        }

        member.addStudyPost(studyPost);
        study.addStudyPost(studyPost);
        studyPost = studyPostRepository.save(studyPost);

        List<MultipartFile> images = postRequestDTO.getImages();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = s3ImageService.upload(image);
                StudyPostImage studyPostImage = new StudyPostImage(imageUrl);
                studyPost.addImage(studyPostImage); // image id가 저장되지 않음
                studyPostImage = studyPostImageRepository.save(studyPostImage);
                studyPost.updateImage(studyPostImage); // image id 저장
            }
        }

        member.updateStudyPost(studyPost);
        study.updateStudyPost(studyPost);

        return StudyPostResDTO.PostPreviewDTO.toDTO(studyPost);
    }

    private StudyPost createPostImages(StudyPostRequestDTO.PostDTO postRequestDTO, StudyPost studyPost, Member member, Study study) {

        List<MultipartFile> images = postRequestDTO.getImages();
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> {
                String imageUrl = s3ImageService.upload(image);
                StudyPostImage studyPostImage = new StudyPostImage(imageUrl);
                studyPostImage = studyPostImageRepository.save(studyPostImage);
                studyPost.addImage(studyPostImage);
            });
        }
        member.updateStudyPost(studyPost);
        study.updateStudyPost(studyPost);
        return studyPostRepository.save(studyPost);

    }

    @Override
    public StudyPostResDTO.PostPreviewDTO deletePost(Long studyId, Long postId) {

        //=== Exception ===//
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        StudyPost studyPost = studyPostRepository.findById(postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 로그인한 회원과 게시글 작성자가 일치하는지 확인
        //Member member = memberRepository.findById(memberId)
        //        .orElseThrow(() -> new StudyHandler(ErrorStatus._MEMBER_NOT_FOUND));

        //=== Feature ===//
        List<StudyPostImage> imagesCopy = new ArrayList<>(studyPost.getImages());
        imagesCopy.forEach(image -> {
            studyPost.deleteImage(image);
            s3ImageService.deleteImageFromS3(image.getUrl());
            studyPostImageRepository.delete(image);
        });
        List<StudyPostComment> commentsCopy = new ArrayList<>(studyPost.getComments());
        commentsCopy.forEach(comment -> {
            studyPost.deleteComment(comment);
            studyPostCommentRepository.delete(comment);
        });
        List<StudyLikedPost> likedPostsCopy = new ArrayList<>(studyPost.getLikedPosts());
        likedPostsCopy.forEach(likedPost -> {
            studyPost.deleteLikedPost(likedPost);
            studyLikedPostRepository.delete(likedPost);
        });

        //member.deleteStudyPost(studyPost);
        study.deleteStudyPost(studyPost);
        studyPostRepository.delete(studyPost);

        return StudyPostResDTO.PostPreviewDTO.toDTO(studyPost);
    }
}

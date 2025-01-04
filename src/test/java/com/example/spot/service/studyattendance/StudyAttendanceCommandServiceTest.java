package com.example.spot.service.studyattendance;

import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Quiz;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.mapping.MemberAttendance;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.*;
import com.example.spot.service.memberstudy.MemberStudyCommandServiceImpl;
import com.example.spot.web.dto.memberstudy.request.StudyQuizRequestDTO;
import com.example.spot.web.dto.memberstudy.response.StudyQuizResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyAttendanceCommandServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StudyRepository studyRepository;
    @Mock
    private MemberStudyRepository memberStudyRepository;

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private MemberAttendanceRepository memberAttendanceRepository;

    @InjectMocks
    private MemberStudyCommandServiceImpl memberStudyCommandService;

    private static Study study;
    private static Member member1;
    private static Member member2;
    private static Member owner;
    private static MemberStudy member1Study;
    private static MemberStudy ownerStudy;
    private static Schedule schedule;
    private static Quiz quiz1;
    private static MemberAttendance member1Attendance;
    private static MemberAttendance ownerAttendance;

    @Mock
    private static Quiz quiz2;

    private static final LocalDate date = LocalDate.now();
    private static final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        initMember();
        initStudy();
        initMemberStudy();
        initSchedule();

        when(memberRepository.findById(member1.getId())).thenReturn(Optional.of(member1));
        when(memberRepository.findById(member2.getId())).thenReturn(Optional.of(member2));
        when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        when(memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(owner.getId(), 1L, Boolean.TRUE))
                .thenReturn(Optional.of(ownerStudy));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member1.getId(), 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(member1Study));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(owner.getId(), 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(ownerStudy));
        when(memberStudyRepository.findAllByStudyIdAndStatus(1L, ApplicationStatus.APPROVED))
                .thenReturn(List.of(member1Study, ownerStudy));

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
    }

    @Test
    @DisplayName("스터디 퀴즈 생성 - (성공)")
    void createAttendanceQuiz_Success() {

        // given
        Long studyId = 1L;

        // 사용자 인증 정보 생성
        StudyQuizRequestDTO.QuizDTO quizRequestDTO = getQuizDTO(owner);

        LocalDateTime startOfDay = quizRequestDTO.getCreatedAt().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = quizRequestDTO.getCreatedAt().withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);

        when(quizRepository.findAllByScheduleIdAndCreatedAtBetween(schedule.getId(), startOfDay, endOfDay))
                .thenReturn(List.of());
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz2);

        // when
        StudyQuizResponseDTO.QuizDTO result = memberStudyCommandService.createAttendanceQuiz(studyId, schedule.getId(), quizRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuestion()).isEqualTo("최고의 스터디 앱은?");
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    @DisplayName("스터디 퀴즈 생성 - 스터디가 존재하지 않는 경우 (실패)")
    void createAttendanceQuiz_StudyNotFound_Fail() {

        // given
        Long studyId = 2L;
        StudyQuizRequestDTO.QuizDTO quizRequestDTO = getQuizDTO(owner);

        LocalDateTime startOfDay = quizRequestDTO.getCreatedAt().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = quizRequestDTO.getCreatedAt().withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);

        when(quizRepository.findAllByScheduleIdAndCreatedAtBetween(schedule.getId(), startOfDay, endOfDay))
                .thenReturn(List.of());
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz2);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.createAttendanceQuiz(studyId, schedule.getId(), quizRequestDTO));
    }

    @Test
    @DisplayName("스터디 퀴즈 생성 - 스터디 회원이 아닌 경우 (실패)")
    void createAttendanceQuiz_NotStudyMember_Fail() {

        // given
        Long studyId = 1L;
        StudyQuizRequestDTO.QuizDTO quizRequestDTO = getQuizDTO(member2);

        LocalDateTime startOfDay = quizRequestDTO.getCreatedAt().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = quizRequestDTO.getCreatedAt().withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);

        when(quizRepository.findAllByScheduleIdAndCreatedAtBetween(schedule.getId(), startOfDay, endOfDay))
                .thenReturn(List.of());
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz2);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.createAttendanceQuiz(studyId, schedule.getId(), quizRequestDTO));
    }

    @Test
    @DisplayName("스터디 퀴즈 생성 - 스터디 관리자가 아닌 경우 (실패)")
    void createAttendanceQuiz_NotOwner_Fail() {

        // given
        Long studyId = 1L;
        StudyQuizRequestDTO.QuizDTO quizRequestDTO = getQuizDTO(member1);

        LocalDateTime startOfDay = quizRequestDTO.getCreatedAt().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = quizRequestDTO.getCreatedAt().withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);

        when(quizRepository.findAllByScheduleIdAndCreatedAtBetween(schedule.getId(), startOfDay, endOfDay))
                .thenReturn(List.of());
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz2);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.createAttendanceQuiz(studyId, schedule.getId(), quizRequestDTO));
    }

    @Test
    @DisplayName("스터디 퀴즈 생성 - 이미 요청한 날짜에 스터디 퀴즈가 존재하는 경우 (실패)")
    void createAttendanceQuiz_ScheduleNotFound_Fail() {

        // given
        initMemberAttendance();
        initQuiz();

        Long studyId = 1L;
        StudyQuizRequestDTO.QuizDTO quizRequestDTO = getQuizDTO(member1);

        LocalDateTime startOfDay = quizRequestDTO.getCreatedAt().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = quizRequestDTO.getCreatedAt().withHour(23).withMinute(59).withSecond(59).withNano(999_999_000);

        when(quizRepository.findAllByScheduleIdAndCreatedAtBetween(schedule.getId(), startOfDay, endOfDay))
                .thenReturn(List.of(quiz1));
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz2);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.createAttendanceQuiz(studyId, schedule.getId(), quizRequestDTO));
    }

    private StudyQuizRequestDTO.QuizDTO getQuizDTO(Member member1) {
        // 사용자 인증 정보 생성
        getAuthentication(member1.getId());

        StudyQuizRequestDTO.QuizDTO quizRequestDTO = StudyQuizRequestDTO.QuizDTO
                .builder()
                .createdAt(now)
                .question("question")
                .answer("answer")
                .build();

        quiz2 = Quiz.builder()
                .schedule(schedule)
                .member(member1)
                .question("최고의 스터디 앱은?")
                .answer("SPOT")
                .createdAt(now)
                .build();
        return quizRequestDTO;
    }

    @Test
    void attendantStudy() {
    }

    @Test
    void deleteAttendanceQuiz() {
    }

/*-------------------------------------------------------- Utils ------------------------------------------------------------------------*/

    private static void initMember() {
        member1 = Member.builder()
                .id(1L)
                .scheduleList(new ArrayList<>())
                .build();
        member2 = Member.builder()
                .id(2L)
                .scheduleList(new ArrayList<>())
                .build();
        owner = Member.builder()
                .id(3L)
                .scheduleList(new ArrayList<>())
                .build();
    }

    private static void initStudy() {
        study = Study.builder()
                .gender(Gender.MALE)
                .minAge(20)
                .maxAge(29)
                .fee(10000)
                .profileImage("a.jpg")
                .hasFee(true)
                .isOnline(true)
                .goal("SQLD")
                .introduction("SQLD 자격증 스터디")
                .title("SQLD Master")
                .maxPeople(10L)
                .build();
    }

    private static void initMemberStudy() {
        ownerStudy = MemberStudy.builder()
                .id(1L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(true)
                .introduction("Hi")
                .member(owner)
                .study(study)
                .build();
        member1Study = MemberStudy.builder()
                .id(2L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(false)
                .introduction("Hi")
                .member(member1)
                .study(study)
                .build();
    }

    private static void initSchedule() {
        schedule = Schedule.builder()
                .id(1L)
                .study(study)
                .member(owner)
                .build();
        study.addSchedule(schedule);
        owner.addSchedule(schedule);
    }

    private static void initQuiz() {
        quiz1 = Quiz.builder()
                .schedule(schedule)
                .member(owner)
                .question("최고의 스터디 앱은?")
                .answer("SPOT")
                .build();
        quiz1.addMemberAttendance(member1Attendance);
        quiz1.addMemberAttendance(ownerAttendance);
    }

    private static void initMemberAttendance() {
        member1Attendance = MemberAttendance.builder()
                .isCorrect(true)
                .build();
        member1Attendance.setMember(member1);

        ownerAttendance = MemberAttendance.builder()
                .isCorrect(false)
                .build();
        ownerAttendance.setQuiz(quiz1);
    }

    private static void getAuthentication(Long memberId) {
        String idString = String.valueOf(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(idString, null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
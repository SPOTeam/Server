package com.example.spot.service.studyattendance;

import com.example.spot.domain.Member;
import com.example.spot.domain.Quiz;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Period;
import com.example.spot.domain.mapping.MemberAttendance;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.*;
import com.example.spot.service.memberstudy.MemberStudyQueryServiceImpl;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyAttendanceQueryServiceTest {

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
    private MemberStudyQueryServiceImpl memberStudyQueryService;

    private static Study study;
    private static Member member1;
    private static Member member2;
    private static Member owner;
    private static MemberStudy member1Study;
    private static MemberStudy ownerStudy;
    private static Schedule schedule;
    private static Quiz quiz;
    private static MemberAttendance member1Attendance;
    private static MemberAttendance ownerAttendance;

    private static LocalDate date = LocalDate.now();

    @BeforeEach
    void setUp() {
        initMember();
        initStudy();
        initMemberStudy();
        initSchedule();
        initMemberAttendance();
        initQuiz();

        when(memberRepository.findById(member1.getId())).thenReturn(Optional.of(member1));
        when(memberRepository.findById(member2.getId())).thenReturn(Optional.of(member2));
        when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member1.getId(), 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(member1Study));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(owner.getId(), 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(ownerStudy));
        when(memberStudyRepository.findAllByStudyIdAndStatus(1L, ApplicationStatus.APPROVED))
                .thenReturn(List.of(member1Study, ownerStudy));

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(quizRepository.findAllByScheduleIdAndCreatedAtBetween(schedule.getId(), date.atStartOfDay(), date.atStartOfDay().plusDays(1)))
                .thenReturn(List.of(quiz));
        when(memberAttendanceRepository.findByQuizIdAndMemberId(quiz.getId(), member1.getId()))
                .thenReturn(List.of(member1Attendance));
        when(memberAttendanceRepository.findByQuizIdAndMemberId(quiz.getId(), owner.getId()))
                .thenReturn(List.of(ownerAttendance));
    }

    @Test
    @DisplayName("회원 출석부 불러오기 - (성공)")
    void getAllAttendances() {

        // given
        Long studyId = 1L;

        // 사용자 인증 정보 생성
        getAuthentication(member1.getId());

        // when
        StudyQuizResponseDTO.AttendanceListDTO result = memberStudyQueryService.getAllAttendances(studyId, schedule.getId(), date);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(schedule.getId());
        assertThat(result.getQuizId()).isEqualTo(quiz.getId());
        assertThat(result.getStudyMembers()).size().isEqualTo(2); // 전체 인원 2명
        assertThat(result.getStudyMembers().stream()
                .filter(StudyQuizResponseDTO.StudyMemberDTO::getIsAttending)
                .toList()).size().isEqualTo(1); // 출석 인원 1명
    }

    @Test
    @DisplayName("출석 퀴즈 불러오기 - (성공)")
    void getAttendanceQuiz() {

        // given
        Long studyId = 1L;

        // 사용자 인증 정보 생성
        getAuthentication(member1.getId());

        // when
        StudyQuizResponseDTO.QuizDTO result = memberStudyQueryService.getAttendanceQuiz(studyId, schedule.getId(), date);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuizId()).isEqualTo(quiz.getId());
        assertThat(result.getQuestion()).isEqualTo("최고의 스터디 앱은?");
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
        quiz = Quiz.builder()
                .schedule(schedule)
                .member(owner)
                .question("최고의 스터디 앱은?")
                .answer("SPOT")
                .build();
        quiz.addMemberAttendance(member1Attendance);
        quiz.addMemberAttendance(ownerAttendance);
    }

    private static void initMemberAttendance() {
        member1Attendance = MemberAttendance.builder()
                .isCorrect(true)
                .build();
        member1Attendance.setMember(member1);

        ownerAttendance = MemberAttendance.builder()
                .isCorrect(false)
                .build();
        ownerAttendance.setQuiz(quiz);
    }

    private static void getAuthentication(Long memberId) {
        String idString = String.valueOf(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(idString, null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
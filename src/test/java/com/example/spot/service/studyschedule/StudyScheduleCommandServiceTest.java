package com.example.spot.service.studyschedule;

import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Period;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.*;
import com.example.spot.service.memberstudy.MemberStudyCommandServiceImpl;
import com.example.spot.web.dto.memberstudy.request.ScheduleRequestDTO;
import com.example.spot.web.dto.memberstudy.response.ScheduleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyScheduleCommandServiceTest {

    @Mock
    private MemberRepository memberRepository;

    // 스터디 관련 Mock
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private MemberStudyRepository memberStudyRepository;

    // 스터디 일정 관련 Mock
    @Mock
    private ScheduleRepository scheduleRepository;

    // 알림 관련 Mock
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private MemberStudyCommandServiceImpl memberStudyCommandService;

    private static Study study1;
    private static Study study2;
    private static Member member1;
    private static Member member2;
    private static Member owner;
    private static MemberStudy memberStudy1;
    private static MemberStudy memberStudy2;
    private static MemberStudy memberStudy3;
    private static MemberStudy memberStudy4;
    private static Schedule schedule1;
    private static Schedule schedule2;
    private static Pageable pageable;

    @BeforeEach
    void setUp() {

        // 객체 초기화
        initMember();
        initStudy();
        initMemberStudy();
        pageable = PageRequest.of(0, 10);

        // 사전 응답 설정
        when(memberRepository.existsById(member1.getId())).thenReturn(true);
        when(memberRepository.existsById(member2.getId())).thenReturn(true);
        when(memberRepository.existsById(owner.getId())).thenReturn(true);
    }

    @Test
    @DisplayName("스터디 일정 생성 - 기본 일정인 경우 (성공)")
    void addSchedule_None_Success() {
        addScheduleSuccess(Period.NONE);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Daily 일정인 경우 (성공)")
    void addSchedule_Daily_Success() {
        addScheduleSuccess(Period.DAILY);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Weekly 일정인 경우(성공)")
    void addSchedule_Weekly() {
        addScheduleSuccess(Period.WEEKLY);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Biweekly 일정인 경우")
    void addSchedule_Biweekly() {
        addScheduleSuccess(Period.BIWEEKLY);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Monthly 일정인 경우")
    void addSchedule_Monthly() {
        addScheduleSuccess(Period.MONTHLY);
    }

    private void addScheduleSuccess(Period weekly) {
        // given
        Long studyId = 1L;
        Long memberId = 1L;

        ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO = ScheduleRequestDTO.ScheduleDTO
                .builder()
                .title("일정1")
                .location("서울특별시")
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusHours(2))
                .isAllDay(false)
                .period(weekly)
                .build();

        // 사용자 인증 정보 생성
        getAuthentication(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member1));
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study1));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(memberStudy2));
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(memberStudy1, memberStudy2));

        // when
        ScheduleResponseDTO.ScheduleDTO result = memberStudyCommandService.addSchedule(studyId, scheduleRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("일정1");
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("스터디 일정 생성 - 스터디 회원이 아닌 경우 (실패)")
    void addSchedule_NotStudyMember_Fail() {
        Long studyId = 1L;
        Long memberId = 2L;
        addScheduleFail(LocalDateTime.now().plusHours(2), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Daily Period 오류 (실패)")
    void addSchedule_Daily_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        addScheduleFail(LocalDateTime.now().plusDays(1), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Weekly Period 오류 (실패)")
    void addSchedule_Weekly_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        addScheduleFail(LocalDateTime.now().plusWeeks(1), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Biweekly Period 오류 (실패)")
    void addSchedule_Biweekly_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        addScheduleFail(LocalDateTime.now().plusWeeks(2), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Monthly Period 오류 (실패)")
    void addSchedule_Monthly_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        addScheduleFail(LocalDateTime.now().plusMonths(2), memberId, studyId);
    }

    private void addScheduleFail(LocalDateTime finishedAt, Long memberId, Long studyId) {
        // given
        ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO = ScheduleRequestDTO.ScheduleDTO
                .builder()
                .title("일정1")
                .location("서울특별시")
                .startedAt(LocalDateTime.now())
                .finishedAt(finishedAt)
                .isAllDay(false)
                .period(Period.NONE)
                .build();

        // 사용자 인증 정보 생성
        getAuthentication(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member1));
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study1));
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(memberStudy1, memberStudy2));

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.addSchedule(studyId, scheduleRequestDTO));
    }

    @Test
    void modSchedule() {
    }

    @Test
    void createAttendanceQuiz() {
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
                .build();
        member2 = Member.builder()
                .id(2L)
                .build();
        owner = Member.builder()
                .id(3L)
                .build();
    }

    private static void initStudy() {
        study1 = Study.builder()
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
        study2 = Study.builder()
                .gender(Gender.FEMALE)
                .minAge(1)
                .maxAge(100)
                .fee(0)
                .profileImage("a.jpg")
                .hasFee(false)
                .isOnline(false)
                .goal("Spring Study")
                .introduction("스프링 스터디")
                .title("SPR")
                .maxPeople(10L)
                .build();
    }

    private static void initMemberStudy() {
        memberStudy1 = MemberStudy.builder()
                .id(1L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(true)
                .introduction("Hi")
                .member(owner)
                .study(study1)
                .build();
        memberStudy2 = MemberStudy.builder()
                .id(2L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(false)
                .introduction("Hi")
                .member(member1)
                .study(study1)
                .build();
        memberStudy3 = MemberStudy.builder()
                .id(3L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(true)
                .introduction("Hi")
                .member(owner)
                .study(study2)
                .build();
        memberStudy4 = MemberStudy.builder()
                .id(4L)
                .status(ApplicationStatus.APPLIED)
                .isOwned(false)
                .introduction("Hi")
                .member(member2)
                .study(study2)
                .build();
    }

    private static void getAuthentication(Long memberId) {
        String idString = String.valueOf(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(idString, null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
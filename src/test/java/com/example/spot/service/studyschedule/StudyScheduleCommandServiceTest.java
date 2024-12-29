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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private static Member member1;
    private static Member member2;
    private static Member owner;
    private static MemberStudy memberStudy1;
    private static MemberStudy memberStudy2;

    @Mock
    private static Schedule schedule;


    @BeforeEach
    void setUp() {
        initMember();
        initStudy();
        initMemberStudy();
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

    private void addScheduleSuccess(Period period) {

        // given
        Long studyId = 1L;
        Long memberId = 1L;

        LocalDateTime startedAt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO = ScheduleRequestDTO.ScheduleDTO
                .builder()
                .title("일정")
                .location("서울특별시")
                .startedAt(startedAt)
                .finishedAt(startedAt.plusHours(3))
                .isAllDay(false)
                .period(period)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .study(study1)
                .member(owner)
                .title("일정")
                .location("서울특별시")
                .startedAt(startedAt)
                .finishedAt(startedAt.plusHours(3))
                .isAllDay(false)
                .period(period)
                .build();

        study1.addSchedule(schedule);
        owner.addSchedule(schedule);

        // 사용자 인증 정보 생성
        getAuthentication(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member1));
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study1));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(memberStudy2));
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(memberStudy1, memberStudy2));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        // when
        ScheduleResponseDTO.ScheduleDTO result = memberStudyCommandService.addSchedule(studyId, scheduleRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("일정");
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("스터디 일정 생성 - 스터디 회원이 아닌 경우 (실패)")
    void addSchedule_NotStudyMember_Fail() {
        Long studyId = 1L;
        Long memberId = 2L;
        LocalDateTime startedAt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        addScheduleFail(startedAt, startedAt.plusHours(2), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Daily Period 오류 (실패)")
    void addSchedule_Daily_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        LocalDateTime startedAt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        addScheduleFail(startedAt, startedAt.plusDays(1), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Weekly Period 오류 (실패)")
    void addSchedule_Weekly_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        LocalDateTime startedAt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        addScheduleFail(startedAt, startedAt.plusWeeks(1), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Biweekly Period 오류 (실패)")
    void addSchedule_Biweekly_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        LocalDateTime startedAt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        addScheduleFail(startedAt, startedAt.plusWeeks(2), memberId, studyId);
    }

    @Test
    @DisplayName("스터디 일정 생성 - Monthly Period 오류 (실패)")
    void addSchedule_Monthly_Fail() {
        Long studyId = 1L;
        Long memberId = 1L;
        LocalDateTime startedAt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        addScheduleFail(startedAt, startedAt.plusMonths(1), memberId, studyId);
    }

    private void addScheduleFail(LocalDateTime startedAt, LocalDateTime finishedAt, Long memberId, Long studyId) {

        // given
        ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO = ScheduleRequestDTO.ScheduleDTO
                .builder()
                .title("일정")
                .location("서울특별시")
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .isAllDay(false)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .study(study1)
                .member(owner)
                .title("일정")
                .location("서울특별시")
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .isAllDay(false)
                .build();

        study1.addSchedule(schedule);
        owner.addSchedule(schedule);

        // 사용자 인증 정보 생성
        getAuthentication(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member1));
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study1));
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(memberStudy1, memberStudy2));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.addSchedule(studyId, scheduleRequestDTO));
    }

    @Test
    @DisplayName("스터디 일정 수정 - (성공)")
    void modSchedule_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long scheduleId = 1L;

        ScheduleRequestDTO.ScheduleDTO scheduleModDTO = getScheduleModDTO(memberId, scheduleId, studyId, member1, study1);

        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(memberStudy2));
        when(scheduleRepository.findByIdAndMemberId(scheduleId, memberId))
                .thenReturn(Optional.of(schedule));
        when(scheduleRepository.findByIdAndStudyId(scheduleId, studyId))
                .thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);

        // when
        ScheduleResponseDTO.ScheduleDTO result = memberStudyCommandService.modSchedule(studyId, scheduleId, scheduleModDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 일정");
        assertThat(study1.getSchedules()).isNotEmpty();
        assertThat(member1.getScheduleList()).isNotEmpty();
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    @DisplayName("스터디 일정 수정 - 스터디 회원이 아닌 경우 (실패)")
    void modSchedule_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long scheduleId = 1L;

        ScheduleRequestDTO.ScheduleDTO scheduleModDTO = getScheduleModDTO(memberId, scheduleId, studyId, member2, study1);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.modSchedule(studyId, scheduleId, scheduleModDTO));
    }

    @Test
    @DisplayName("스터디 일정 수정 - 일정 생성자가 아닌 경우 (실패)")
    void modSchedule_NotCreator_Fail() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long scheduleId = 1L;

        ScheduleRequestDTO.ScheduleDTO scheduleModDTO = getScheduleModDTO(memberId, scheduleId, studyId, member1, study1);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.modSchedule(studyId, scheduleId, scheduleModDTO));
    }

    @Test
    @DisplayName("스터디 일정 수정 - 해당 스터디의 일정이 아닌 경우 (실패)")
    void modSchedule_NotStudySchedule_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 2L;
        Long scheduleId = 1L;

        ScheduleRequestDTO.ScheduleDTO scheduleModDTO = getScheduleModDTO(memberId, scheduleId, studyId, member1, study1);

        // when & then
        assertThrows(StudyHandler.class, () -> memberStudyCommandService.modSchedule(studyId, scheduleId, scheduleModDTO));
    }

    private ScheduleRequestDTO.ScheduleDTO getScheduleModDTO(
            Long memberId, Long scheduleId, Long studyId, Member member, Study study) {

        getAuthentication(memberId);
        LocalDateTime startedAt = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        ScheduleRequestDTO.ScheduleDTO scheduleModDTO = ScheduleRequestDTO.ScheduleDTO
                .builder()
                .title("수정된 일정")
                .location("서울특별시")
                .startedAt(startedAt)
                .finishedAt(startedAt.plusHours(3))
                .isAllDay(false)
                .period(Period.NONE)
                .build();

        schedule = Schedule.builder()
                .id(scheduleId)
                .study(study)
                .member(member)
                .title("일정")
                .startedAt(startedAt)
                .finishedAt(startedAt.plusHours(2))
                .isAllDay(false)
                .period(Period.WEEKLY)
                .build();

        study.addSchedule(schedule);
        member.addSchedule(schedule);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        return scheduleModDTO;
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
    }

    private static void getAuthentication(Long memberId) {
        String idString = String.valueOf(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(idString, null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
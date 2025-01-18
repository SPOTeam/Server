package com.example.spot.service.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.web.dto.study.request.StudyJoinRequestDTO;
import com.example.spot.web.dto.study.response.StudyJoinResponseDTO;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyCommandServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StudyRepository studyRepository;
    @Mock
    private MemberStudyRepository memberStudyRepository;

    @InjectMocks
    private StudyCommandServiceImpl studyCommandService;

    private static Study study;
    private static Member member1;
    private static Member member2;
    private static Member owner;
    private static MemberStudy member1Study;
    private static MemberStudy ownerStudy;

    @Mock
    private static MemberStudy member2Study;

    @Mock
    private static Study newStudy;

    @BeforeEach
    void setUp() {
        initMember();
        initStudy();
        initMemberStudy();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(owner));

        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(1L, 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(member1Study));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(3L, 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(ownerStudy));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(3L, 1L, true))
                .thenReturn(Optional.of(ownerStudy));
    }

    @Test
    @DisplayName("스터디 신청 - (성공)")
    void applyToStudy_Success() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;

        getAuthentication(memberId);

        StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO = StudyJoinRequestDTO.StudyJoinDTO.builder()
                .introduction("Hi")
                .build();

        MemberStudy memberStudy = MemberStudy.builder()
                .id(3L)
                .member(member2)
                .study(study)
                .status(ApplicationStatus.APPLIED)
                .isOwned(false)
                .introduction(studyJoinRequestDTO.getIntroduction())
                .build();

        when(memberStudyRepository.countByStatusAndStudyId(ApplicationStatus.APPROVED, studyId))
                .thenReturn(2L);
        when(memberStudyRepository.findByMemberIdAndStatusNot(memberId, ApplicationStatus.REJECTED))
                .thenReturn(List.of());
        when(memberStudyRepository.save(any(MemberStudy.class)))
                .thenReturn(memberStudy);

        // when
        StudyJoinResponseDTO.JoinDTO result = studyCommandService.applyToStudy(studyId, studyJoinRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        verify(memberStudyRepository, times(1)).save(any(MemberStudy.class));
    }

    @Test
    @DisplayName("스터디 신청 - 이미 스터디에 신청했거나 스터디 회원인 경우(실패)")
    void applyToStudy_StudyMember_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;

        getAuthentication(memberId);

        StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO = StudyJoinRequestDTO.StudyJoinDTO.builder()
                .introduction("Hi")
                .build();

        MemberStudy memberStudy = MemberStudy.builder()
                .id(3L)
                .member(member1)
                .study(study)
                .status(ApplicationStatus.APPLIED)
                .isOwned(false)
                .introduction(studyJoinRequestDTO.getIntroduction())
                .build();

        when(memberStudyRepository.countByStatusAndStudyId(ApplicationStatus.APPROVED, studyId))
                .thenReturn(2L);
        when(memberStudyRepository.findByMemberIdAndStatusNot(memberId, ApplicationStatus.REJECTED))
                .thenReturn(List.of(member1Study));
        when(memberStudyRepository.save(any(MemberStudy.class)))
                .thenReturn(memberStudy);

        // when & then
        assertThrows(StudyHandler.class, () -> studyCommandService.applyToStudy(studyId, studyJoinRequestDTO));
    }

    @Test
    @DisplayName("스터디 신청 - 모집중인 스터디가 아닌 경우(실패)")
    void applyToStudy_NotRecruitingStudy_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 2L;

        getAuthentication(memberId);

        Study study = Study.builder()
                .title("마감된 스터디")
                .maxPeople(1L)
                .build();

        StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO = StudyJoinRequestDTO.StudyJoinDTO.builder()
                .introduction("Hi")
                .build();

        MemberStudy memberStudy = MemberStudy.builder()
                .id(3L)
                .member(member2)
                .study(study)
                .status(ApplicationStatus.APPLIED)
                .isOwned(false)
                .introduction(studyJoinRequestDTO.getIntroduction())
                .build();

        when(memberStudyRepository.countByStatusAndStudyId(ApplicationStatus.APPROVED, studyId))
                .thenReturn(1L);
        when(memberStudyRepository.findByMemberIdAndStatusNot(memberId, ApplicationStatus.REJECTED))
                .thenReturn(List.of());
        when(memberStudyRepository.save(any(MemberStudy.class)))
                .thenReturn(memberStudy);

        // when & then
        assertThrows(StudyHandler.class, () -> studyCommandService.applyToStudy(studyId, studyJoinRequestDTO));
    }

    @Test
    void registerStudy() {
    }

    @Test
    void likeStudy() {
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

    private static void getAuthentication(Long memberId) {
        String idString = String.valueOf(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(idString, null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
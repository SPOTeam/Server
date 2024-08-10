package com.example.spot.service.study;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.example.spot.domain.Member;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberTheme;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.MemberThemeRepository;
import com.example.spot.repository.PreferredRegionRepository;
import com.example.spot.repository.PreferredStudyRepository;
import com.example.spot.repository.RegionStudyRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.repository.StudyThemeRepository;
import com.example.spot.repository.ThemeRepository;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    // 스터디 관련 조회
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private MemberStudyRepository memberStudyRepository;
    @Mock
    private PreferredStudyRepository preferredStudyRepository;

    // 관심사 관련 조회
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private StudyThemeRepository studyThemeRepository;
    @Mock
    private MemberThemeRepository memberThemeRepository;

    // 지역 관련 조회
    @Mock
    private PreferredRegionRepository preferredRegionRepository;
    @Mock
    private RegionStudyRepository regionStudyRepository;

    @InjectMocks
    private StudyQueryServiceImpl studyQueryService;

    private static Study study1;
    private static Study study2;

    @BeforeAll
    static void setup() {
        // 모든 테스트에서 사용할 Study 객체를 미리 생성
        study1 = Study.builder()
            .gender(Gender.MALE)
            .minAge(18)
            .maxAge(35)
            .fee(1000)
            .profileImage("profile1.jpg")
            .hasFee(true)
            .isOnline(true)
            .goal("Learn English")
            .introduction("This is an English study group")
            .title("English Study Group")
            .maxPeople(10L)
            .build();

        study2 = Study.builder()
            .gender(Gender.FEMALE)
            .minAge(18)
            .maxAge(35)
            .fee(2000)
            .profileImage("profile2.jpg")
            .hasFee(true)
            .isOnline(false)
            .goal("Win a competition")
            .introduction("This is a competition study group")
            .title("Competition Study Group")
            .maxPeople(15L)
            .build();
    }

    @BeforeEach
    void initMocks() {
        // 각 테스트 메서드에서 사용할 mock 설정
        when(studyRepository.findByStudyTheme(anyList())).thenReturn(List.of(study1, study2));
    }
    @Test
    void getStudyInfo() {
        // given

        // when

        // then
    }

    @Test
    void getMyPageStudyCount() {
    }

    @Test
    void findRecommendStudies() {
        // given
        Member member = Member.builder()
            .id(1L)
            .build();
        Long memberId = 1L;

        Theme theme1 = Theme.builder()
            .id(1L)
            .studyTheme(ThemeType.어학)
            .build();

        Theme theme2 = Theme.builder()
            .id(2L)
            .studyTheme(ThemeType.공모전)
            .build();

        List<Theme> themeList = List.of(theme1, theme2);

        MemberTheme memberTheme1 = MemberTheme.builder().member(member).theme(theme1).build();
        MemberTheme memberTheme2 = MemberTheme.builder().member(member).theme(theme2).build();

        // Mock the memberThemeRepository to return a list of MemberTheme
        when(memberThemeRepository.findAllByMemberId(memberId)).thenReturn(List.of(memberTheme1, memberTheme2));

        StudyTheme studyTheme1 = new StudyTheme(theme1, study1);
        StudyTheme studyTheme2 = new StudyTheme(theme2, study2);

        when(studyThemeRepository.findAllByTheme(theme1)).thenReturn(List.of(studyTheme1));
        when(studyThemeRepository.findAllByTheme(theme2)).thenReturn(List.of(studyTheme2));

        // Mocking the studyRepository to return studies based on the study themes
        when(studyRepository.findByStudyTheme(anyList())).thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findRecommendStudies(memberId);

        // then
        assertNotNull(result);
        assertEquals(2, result.getSize());  // Assuming StudyPreviewDTO has a getStudies method
        verify(memberThemeRepository).findAllByMemberId(memberId);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
        verify(studyRepository).findByStudyTheme(anyList());
    }

    @Test
    void findInterestStudiesByConditionsAll() {
    }

    @Test
    void findInterestStudiesByConditionsSpecific() {
    }

    @Test
    void findInterestRegionStudiesByConditionsAll() {
    }

    @Test
    void findInterestRegionStudiesByConditionsSpecific() {
    }

    @Test
    void findRecruitingStudiesByConditions() {
    }

    @Test
    void findLikedStudies() {
    }

    @Test
    void findStudiesByKeyword() {
    }

    @Test
    void findStudiesByTheme() {
    }

    @Test
    void findOngoingStudiesByMemberId() {
    }

    @Test
    void findAppliedStudies() {
    }

    @Test
    void findMyRecruitingStudies() {
    }
}
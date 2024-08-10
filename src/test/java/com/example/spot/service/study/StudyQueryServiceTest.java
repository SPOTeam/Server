package com.example.spot.service.study;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.example.spot.domain.Member;
import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.StudyLikeStatus;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.MemberTheme;
import com.example.spot.domain.mapping.PreferredRegion;
import com.example.spot.domain.mapping.PreferredStudy;
import com.example.spot.domain.mapping.RegionStudy;
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
import com.example.spot.web.dto.search.SearchRequestDTO.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.security.core.parameters.P;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    @DisplayName("추천 스터디 조회 - 추천 스터디가 있는 경우")
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
    @DisplayName("내 전체 관심사 스터디 조회 - 내 전체 관심사에 해당하는 스터디가 있는 경우")
    void findInterestStudiesByConditionsAll() {
        // given
        Member member = Member.builder()
            .id(1L)
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        Theme theme1 = Theme.builder()
            .id(1L)
            .studyTheme(ThemeType.어학)
            .build();

        Theme theme2 = Theme.builder()
            .id(2L)
            .studyTheme(ThemeType.공모전)
            .build();

        StudySortBy sortBy = StudySortBy.ALL;

        SearchRequestStudyDTO request = SearchRequestStudyDTO.builder()
            .gender(Gender.MALE)
            .minAge(20)
            .maxAge(40)
            .fee(10000)
            .isOnline(true)
            .hasFee(true)
            .build();

        MemberTheme memberTheme1 = MemberTheme.builder().member(member).theme(theme1).build();
        MemberTheme memberTheme2 = MemberTheme.builder().member(member).theme(theme2).build();

        StudyTheme studyTheme1 = new StudyTheme(theme1, study1);
        StudyTheme studyTheme2 = new StudyTheme(theme2, study2);

        // Mock conditions
        Map<String, Object> searchConditions = new HashMap<>();
        searchConditions.put("gender", Gender.MALE);
        searchConditions.put("minAge", 20);
        searchConditions.put("maxAge", 40);
        searchConditions.put("isOnline", true);
        searchConditions.put("hasFee", true);
        searchConditions.put("fee", 10000);

        when(memberThemeRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(theme1))
            .thenReturn(List.of(studyTheme1));
        when(studyThemeRepository.findAllByTheme(theme2))
            .thenReturn(List.of(studyTheme2));

        when(studyRepository.countStudyByConditionsAndThemeTypes(searchConditions, List.of(studyTheme1, studyTheme2), sortBy))
            .thenReturn(2L);

        when(studyRepository.findStudyByConditionsAndThemeTypes(
            searchConditions, sortBy, pageable, List.of(studyTheme1, studyTheme2)))
            .thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsAll(pageable, member.getId(), request, sortBy);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());  // Assuming StudyPreviewDTO has a getSize method
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
        verify(studyRepository).countStudyByConditionsAndThemeTypes(searchConditions, List.of(studyTheme1, studyTheme2), sortBy);
        verify(studyRepository).findStudyByConditionsAndThemeTypes(searchConditions, sortBy, pageable, List.of(studyTheme1, studyTheme2));

    }

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 내 특정 관심사에 해당하는 스터디가 있는 경우")
    void findInterestStudiesByConditionsSpecific() {
        // given
        Member member = Member.builder()
            .id(1L)
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        ThemeType themeType = ThemeType.어학;

        Theme theme1 = Theme.builder()
            .id(1L)
            .studyTheme(ThemeType.어학)
            .build();

        Theme theme2 = Theme.builder()
            .id(2L)
            .studyTheme(ThemeType.공모전)
            .build();

        StudySortBy sortBy = StudySortBy.ALL;

        SearchRequestStudyDTO request = SearchRequestStudyDTO.builder()
            .gender(Gender.MALE)
            .minAge(20)
            .maxAge(40)
            .fee(10000)
            .isOnline(true)
            .hasFee(true)
            .build();

        MemberTheme memberTheme1 = MemberTheme.builder().member(member).theme(theme1).build();
        MemberTheme memberTheme2 = MemberTheme.builder().member(member).theme(theme2).build();

        StudyTheme studyTheme1 = new StudyTheme(theme1, study1);
        StudyTheme studyTheme2 = new StudyTheme(theme2, study2);

        // Mock conditions
        Map<String, Object> searchConditions = new HashMap<>();
        searchConditions.put("gender", Gender.MALE);
        searchConditions.put("minAge", 20);
        searchConditions.put("maxAge", 40);
        searchConditions.put("isOnline", true);
        searchConditions.put("hasFee", true);
        searchConditions.put("fee", 10000);

        when(memberThemeRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(memberTheme1));
        when(studyThemeRepository.findAllByTheme(theme1))
            .thenReturn(List.of(studyTheme1));

        // Only studyTheme1 should match
        when(studyRepository.countStudyByConditionsAndThemeTypes(searchConditions, List.of(studyTheme1), sortBy))
            .thenReturn(1L);

        // Adjusting the mock to match the specific test data
        when(studyRepository.findStudyByConditionsAndThemeTypes(
            searchConditions, sortBy, pageable, List.of(studyTheme1)))
            .thenReturn(List.of(study1));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsSpecific(pageable, member.getId(), request, themeType, sortBy);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());  // Verify the count matches expected result
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository).findAllByTheme(theme1);  // Ensure the correct theme is queried
        verify(studyRepository).countStudyByConditionsAndThemeTypes(searchConditions, List.of(studyTheme1), sortBy);
        verify(studyRepository).findStudyByConditionsAndThemeTypes(searchConditions, sortBy, pageable, List.of(studyTheme1));
    }

    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 내 전체 관심 지역에 해당하는 스터디가 있는 경우")
    void findInterestRegionStudiesByConditionsAll() {
        // given
        Member member = Member.builder()
            .id(1L)
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        Region region1 = Region.builder()
            .province("경기도")
            .neighborhood("송산면")
            .district("화성시")
            .code("4159034000")
            .build();

        Region region2 = Region.builder()
            .province("경기도")
            .neighborhood("봉담읍")
            .district("화성시")
            .code("4159025300")
            .build();

        StudySortBy sortBy = StudySortBy.ALL;

        SearchRequestStudyDTO request = SearchRequestStudyDTO.builder()
            .gender(Gender.MALE)
            .minAge(20)
            .maxAge(40)
            .fee(10000)
            .isOnline(true)
            .hasFee(true)
            .build();

        PreferredRegion preferredRegion1 = PreferredRegion.builder().member(member).region(region1).build();
        PreferredRegion preferredRegion2 = PreferredRegion.builder().member(member).region(region2).build();

        RegionStudy regionStudy1 = RegionStudy.builder().region(region1).study(study1).build();
        RegionStudy regionStudy2 = RegionStudy.builder().region(region2).study(study2).build();

        // Mock conditions
        Map<String, Object> searchConditions = new HashMap<>();
        searchConditions.put("gender", Gender.MALE);
        searchConditions.put("minAge", 20);
        searchConditions.put("maxAge", 40);
        searchConditions.put("isOnline", true);
        searchConditions.put("hasFee", true);
        searchConditions.put("fee", 10000);

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(region1))
            .thenReturn(List.of(regionStudy1));
        when(regionStudyRepository.findAllByRegion(region2))
            .thenReturn(List.of(regionStudy2));

        when(studyRepository.countStudyByConditionsAndRegionStudies(searchConditions, List.of(regionStudy1, regionStudy2), sortBy))
            .thenReturn(2L);

        when(studyRepository.findStudyByConditionsAndRegionStudies(
            searchConditions, sortBy, pageable, List.of(regionStudy1, regionStudy2)))
            .thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsAll(pageable, member.getId(), request, sortBy);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());  // Assuming StudyPreviewDTO has a getSize method
        verify(preferredRegionRepository).findAllByMemberId(member.getId());
        verify(regionStudyRepository, times(1)).findAllByRegion(region1);
        verify(regionStudyRepository, times(1)).findAllByRegion(region2);
        verify(studyRepository).countStudyByConditionsAndRegionStudies(searchConditions, List.of(regionStudy1, regionStudy2), sortBy);
        verify(studyRepository).findStudyByConditionsAndRegionStudies(searchConditions, sortBy, pageable, List.of(regionStudy1, regionStudy2));
    }

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 내 특정 관심 지역에 해당하는 스터디가 있는 경우")
    void findInterestRegionStudiesByConditionsSpecific() {
        // given
        Member member = Member.builder()
            .id(1L)
            .build();

        String regionCode = "4159034000";

        Pageable pageable = PageRequest.of(0, 10);

        Region region1 = Region.builder()
            .province("경기도")
            .neighborhood("송산면")
            .district("화성시")
            .code("4159034000")
            .build();

        Region region2 = Region.builder()
            .province("경기도")
            .neighborhood("봉담읍")
            .district("화성시")
            .code("4159025300")
            .build();

        StudySortBy sortBy = StudySortBy.ALL;

        SearchRequestStudyDTO request = SearchRequestStudyDTO.builder()
            .gender(Gender.MALE)
            .minAge(20)
            .maxAge(40)
            .fee(10000)
            .isOnline(true)
            .hasFee(true)
            .build();
        PreferredRegion preferredRegion1 = PreferredRegion.builder().member(member).region(region1).build();
        PreferredRegion preferredRegion2 = PreferredRegion.builder().member(member).region(region2).build();

        RegionStudy regionStudy1 = RegionStudy.builder().region(region1).study(study1).build();
        RegionStudy regionStudy2 = RegionStudy.builder().region(region2).study(study2).build();

        // Mock conditions
        Map<String, Object> searchConditions = new HashMap<>();
        searchConditions.put("gender", Gender.MALE);
        searchConditions.put("minAge", 20);
        searchConditions.put("maxAge", 40);
        searchConditions.put("isOnline", true);
        searchConditions.put("hasFee", true);
        searchConditions.put("fee", 10000);

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(preferredRegion1));
        when(regionStudyRepository.findAllByRegion(region1))
            .thenReturn(List.of(regionStudy1));

        // Only studyTheme1 should match
        when(studyRepository.countStudyByConditionsAndRegionStudies(searchConditions, List.of(regionStudy1), sortBy))
            .thenReturn(1L);

        // Adjusting the mock to match the specific test data
        when(studyRepository.findStudyByConditionsAndRegionStudies(
            searchConditions, sortBy, pageable, List.of(regionStudy1)))
            .thenReturn(List.of(study1));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsSpecific(pageable, member.getId(), request, regionCode, sortBy);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());  // Verify the count matches expected result
        verify(preferredRegionRepository).findAllByMemberId(member.getId());
        verify(regionStudyRepository).findAllByRegion(region1);  // Ensure the correct theme is queried
        verify(studyRepository).countStudyByConditionsAndRegionStudies(searchConditions, List.of(regionStudy1), sortBy);
        verify(studyRepository).findStudyByConditionsAndRegionStudies(searchConditions, sortBy, pageable, List.of(regionStudy1));
    }

    @Test
    @DisplayName("모집 중 스터디 조회 - 모집 중인 스터디가 있는 경우")
    void findRecruitingStudiesByConditions() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        StudySortBy sortBy = StudySortBy.ALL;

        SearchRequestStudyDTO request = SearchRequestStudyDTO.builder()
            .gender(Gender.FEMALE)
            .minAge(18)
            .maxAge(35)
            .fee(5000)
            .isOnline(true)
            .hasFee(true)
            .build();

        // Mock conditions
        Map<String, Object> searchConditions = new HashMap<>();
        searchConditions.put("gender", Gender.FEMALE);
        searchConditions.put("minAge", 18);
        searchConditions.put("maxAge", 35);
        searchConditions.put("isOnline", true);
        searchConditions.put("hasFee", true);
        searchConditions.put("fee", 5000);

        when(studyRepository.findStudyByConditions(searchConditions, sortBy, pageable))
            .thenReturn(List.of(study1, study2));
        when(studyRepository.countStudyByConditions(searchConditions, sortBy))
            .thenReturn(2L);

        // when
        StudyPreviewDTO result = studyQueryService.findRecruitingStudiesByConditions(pageable, request, sortBy);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());  // Verify the count of elements
        verify(studyRepository).findStudyByConditions(searchConditions, sortBy, pageable);
        verify(studyRepository).countStudyByConditions(searchConditions, sortBy);
    }

    @Test
    @DisplayName("찜한 스터디 조회 - 찜한 스터디가 있는 경우")
    void findLikedStudies() {
        // given
        Member member = Member.builder()
            .id(1L)
            .build();

        PreferredStudy preferredStudy1 = PreferredStudy.builder()
            .member(member)
            .study(study1)
            .studyLikeStatus(StudyLikeStatus.LIKE)
            .build();

        PreferredStudy preferredStudy2 = PreferredStudy.builder()
            .member(member)
            .study(study2)
            .studyLikeStatus(StudyLikeStatus.LIKE)
            .build();

        when(preferredStudyRepository.findByMemberIdOrderByCreatedAtDesc(member.getId()))
            .thenReturn(List.of(preferredStudy1, preferredStudy2));

        // when
        StudyPreviewDTO result = studyQueryService.findLikedStudies(member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(preferredStudyRepository).findByMemberIdOrderByCreatedAtDesc(member.getId());

    }

    @Test
    @DisplayName("키워드로 스터디 검색 - 해당 키워드에 해당하는 스터디가 있는 경우")
    void findStudiesByKeyword() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "English";
        StudySortBy sortBy = StudySortBy.ALL;

        when(studyRepository.findAllByTitleContaining(keyword, sortBy, pageable))
            .thenReturn(List.of(study1));
        when(studyRepository.countAllByTitleContaining(keyword, sortBy))
            .thenReturn(1L);

        // when
        StudyPreviewDTO result = studyQueryService.findStudiesByKeyword(pageable, keyword, sortBy);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(studyRepository).findAllByTitleContaining(keyword, sortBy, pageable);
    }

    @Test
    @DisplayName("테마 별 스터디 검색 - 해당 테마에 해당하는 스터디가 있는 경우")
    void findStudiesByTheme() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ThemeType themeType = ThemeType.어학;
        StudySortBy sortBy = StudySortBy.ALL;
        Theme theme = Theme.builder()
            .id(1L)
            .studyTheme(themeType)
            .build();
        StudyTheme studyTheme = new StudyTheme(theme, study1);

        when(themeRepository.findByStudyTheme(themeType)).thenReturn(Optional.ofNullable(theme));
        when(studyThemeRepository.findAllByTheme(theme)).thenReturn(List.of(studyTheme));

        when(studyRepository.findByStudyTheme(List.of(studyTheme), sortBy, pageable))
            .thenReturn(List.of(study1));
        when(studyRepository.countStudyByStudyTheme(List.of(studyTheme), sortBy))
            .thenReturn(1L);

        // when
        StudyPreviewDTO result = studyQueryService.findStudiesByTheme(pageable, themeType, sortBy);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(themeRepository).findByStudyTheme(themeType);
        verify(studyThemeRepository).findAllByTheme(theme);
        verify(studyRepository).findByStudyTheme(List.of(studyTheme), sortBy, pageable);
        verify(studyRepository).countStudyByStudyTheme(List.of(studyTheme), sortBy);

    }

    @Test
    @DisplayName("내가 참여하고 있는 스터디 조회 - 참여하고 있는 스터디가 있는 경우")
    void findOngoingStudiesByMemberId() {

        // given
        Member member = Member.builder()
            .id(1L)
            .build();
        Pageable pageable = PageRequest.of(0, 10);
        MemberStudy memberStudy1 = MemberStudy.builder()
            .member(member)
            .study(study1)
            .build();
        MemberStudy memberStudy2 = MemberStudy.builder()
            .member(member)
            .study(study2)
            .build();

        when(memberStudyRepository.findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPROVED))
            .thenReturn(List.of(memberStudy1, memberStudy2));
        when(studyRepository.findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable))
            .thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findOngoingStudiesByMemberId(pageable, member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(memberStudyRepository).findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPROVED);
        verify(studyRepository).findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable);

    }

    @Test
    @DisplayName("내가 신청한 스터디 조회 - 신청한 스터디가 있는 경우")
    void findAppliedStudies() {
        // given
        Member member = Member.builder()
            .id(1L)
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        MemberStudy memberStudy1 = MemberStudy.builder()
            .member(member)
            .study(study1)
            .build();
        MemberStudy memberStudy2 = MemberStudy.builder()
            .member(member)
            .study(study2)
            .build();

        when(memberStudyRepository.findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPLIED))
            .thenReturn(List.of(memberStudy1, memberStudy2));
        when(studyRepository.findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable))
            .thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findAppliedStudies(pageable, member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(memberStudyRepository).findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPLIED);
        verify(studyRepository).findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable);

    }

    @Test
    void findMyRecruitingStudies() {
    }
}
package com.example.spot.service.study;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
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
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.MyPageDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO.StudyInfoDTO;
import java.util.Collections;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private static Study study3;
    private static Member member;
    private static Pageable pageable;
    private static Theme theme1;
    private static Theme theme2;
    private static MemberTheme memberTheme1;
    private static MemberTheme memberTheme2;
    private static StudyTheme studyTheme1;
    private static StudyTheme studyTheme2;
    private static SearchRequestStudyDTO request;
    private static Region region1;
    private static Region region2;
    private static PreferredRegion preferredRegion1;
    private static PreferredRegion preferredRegion2;
    private static RegionStudy regionStudy1;
    private static RegionStudy regionStudy2;
    private static PreferredStudy preferredStudy1;
    private static PreferredStudy preferredStudy2;
    private static MemberStudy memberStudy1;
    private static MemberStudy memberStudy2;

    @BeforeEach
    void setUp() {
        initStudy();
        member = getMember();
        pageable = PageRequest.of(0, 10);

        theme1 = getTheme(1L, ThemeType.어학);
        theme2 = getTheme(2L, ThemeType.공모전);

        memberTheme1 = MemberTheme.builder().member(member).theme(theme1).build();
        memberTheme2 = MemberTheme.builder().member(member).theme(theme2).build();

        studyTheme1 = new StudyTheme(theme1, study1);
        studyTheme2 = new StudyTheme(theme2, study2);

        region1 = getRegion("송산면", "4159034000");
        region2 = getRegion("봉담읍", "4159025300");

        preferredRegion1 = PreferredRegion.builder().member(member).region(region1).build();
        preferredRegion2 = PreferredRegion.builder().member(member).region(region2).build();

        regionStudy1 = RegionStudy.builder().region(region1).study(study1).build();
        regionStudy2 = RegionStudy.builder().region(region2).study(study2).build();

        preferredStudy1 = getPreferredStudy(member, study1);
        preferredStudy2 = getPreferredStudy(member, study2);


        memberStudy1 = getMemberStudy(member, study1);
        memberStudy2 = getMemberStudy(member, study2);

        study1.addMemberStudy(memberStudy1);

        request = getSearchRequestStudyDTO();

        // 사용자 인증 정보 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken("1", null, Collections.emptyList());
        // SecurityContext 생성 및 설정
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(any())).thenReturn(List.of(studyTheme1, studyTheme2));
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
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

    /* -------------------------------------------------------- 스터디 상세 정보 조회  ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 상세 정보 조회 - 성공")
    void 스터디_상세_정보_조회_성공() {
        // given
        Long studyId = 1L;

        when(studyRepository.findById(studyId)).thenReturn(Optional.ofNullable(study1));

        // when
        StudyInfoDTO result = studyQueryService.getStudyInfo(studyId);

        // then
        assertNotNull(result);
        assertEquals(result.getStudyId(), study1.getId());
        assertEquals(result.getStudyName(), study1.getTitle());
        verify(studyRepository).findById(studyId);
    }

    @Test
    @DisplayName("스터디 상세 정보 조회 - 찾는 스터디가 없는 경우")
    void 스터디_상세_정보_조회_시_스터디가_없는_경우() {
        // given
        Long studyId = 1L;

        when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.getStudyInfo(studyId);
        });
        verify(studyRepository).findById(studyId);
    }

    @Test
    @DisplayName("스터디 상세 정보 조회 - 스터디의 소유자가 없는 경우 (스터디 삭제..등)")
    void 스터디_상세_정보_조회_시_스터디의_소유자가_없는_경우() {
        // given
        Long studyId = 2L;

        when(studyRepository.findById(studyId)).thenReturn(Optional.ofNullable(study2));

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.getStudyInfo(studyId);
        });
        verify(studyRepository).findById(studyId);
    }

    /* -------------------------------------------------------- 마이페이지 스터디 갯수 조회  ------------------------------------------------------------------------*/

    @Test
    @DisplayName("마이페이지 스터디 갯수 조회 - 성공")
    void 마이페이지_스터디_갯수_조회_성공() {
        // given
        Long memberId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberStudyRepository.countByMemberIdAndStatus(memberId, ApplicationStatus.APPLIED)).thenReturn(2L);
        when(memberStudyRepository.countByMemberIdAndStatus(memberId, ApplicationStatus.APPROVED)).thenReturn(1L);
        when(memberStudyRepository.countByMemberIdAndIsOwned(memberId, true)).thenReturn(3L);

        // when
        MyPageDTO myPageStudyCount = studyQueryService.getMyPageStudyCount(memberId);

        // then
        assertNotNull(myPageStudyCount);
        assertEquals(member.getName(), myPageStudyCount.getName());
        assertEquals(2, myPageStudyCount.getAppliedStudies());
        assertEquals(1, myPageStudyCount.getOngoingStudies());
        assertEquals(3, myPageStudyCount.getMyRecruitingStudies());
    }

    @Test
    @DisplayName("마이페이지 스터디 갯수 조회 - 유효하지 않은 사용자인 경우")
    void 마이페이지_스터디_갯수_조회_시_유효하지_않은_사용자인_경우() {
        // given
        Long memberId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(MemberHandler.class, () -> {
            studyQueryService.getMyPageStudyCount(memberId);
        });
    }

    /* -------------------------------------------------------- 검색 조건 없는 스터디 검색  ------------------------------------------------------------------------*/

    @Test
    @DisplayName("검색 조건 없는 스터디 검색 - 성공")
    void 검색_조건_없는_스터디_검색() {
        // given
        when(studyRepository.findAllStudy(StudySortBy.ALL, pageable)).thenReturn(List.of(study1, study2));
        when(studyRepository.count()).thenReturn(2L);

        // when
        StudyPreviewDTO studies = studyQueryService.findStudies(pageable, StudySortBy.ALL);

        // then
        assertNotNull(studies);
        assertEquals(2, studies.getTotalElements());
        assertEquals(study1.getTitle(), studies.getContent().get(0).getTitle());

    }

    @Test
    @DisplayName("검색 조건 없는 스터디 검색 - 페이징 테스트")
    void 검색_조건_없는_스터디_검색_페이징(){
        //given
        List<Study> studies = List.of(study1, study2);

        when(studyRepository.findAllStudy(any(), any()))
            .thenReturn(studies);
        when(studyRepository.count())
            .thenReturn(2L);

        // when
        StudyPreviewDTO result = studyQueryService.findStudies(
            PageRequest.of(0, 10), StudySortBy.ALL);

        // then
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

    }

    @Test
    @DisplayName("검색 조건 없는 스터디 검색 - 조회된 스터디가 없을 경우")
    void 검색_조건_없는_스터디_검색_시_스터디가_없는_경우() {
        // given
        when(studyRepository.findAllStudy(StudySortBy.ALL, pageable)).thenReturn(List.of());
        when(studyRepository.count()).thenReturn(0L);

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findStudies(pageable, StudySortBy.ALL);
        });
    }

    /* -------------------------------------------------------- 검색 조건 있는 스터디 검색  ------------------------------------------------------------------------*/

    @Test
    @DisplayName("검색 조건 있는 스터디 검색 - 성공")
    void 검색_조건_있는_스터디_검색() {
        // given
        SearchRequestStudyDTO request = getSearchRequestStudyDTO();
        Map<String, Object> conditions = getStringObjectMap();
        when(studyRepository.findAllStudyByConditions(conditions, StudySortBy.ALL, pageable)).thenReturn(List.of(study1, study2));
        when(studyRepository.countStudyByConditions(conditions, StudySortBy.ALL)).thenReturn(2L);

        // when
        StudyPreviewDTO studies = studyQueryService.findStudiesByConditions(pageable, request, StudySortBy.ALL);

        // then
        assertNotNull(studies);
        assertEquals(2, studies.getTotalElements());
        assertEquals(study1.getTitle(), studies.getContent().get(0).getTitle());

    }

    @Test
    @DisplayName("검색 조건 있는 스터디 검색 - 페이징 테스트")
    void 검색_조건_있는_스터디_검색_페이징(){
        //given
        List<Study> studies = List.of(study1, study2);
        SearchRequestStudyDTO request = getSearchRequestStudyDTO();
        Map<String, Object> conditions = getStringObjectMap();
        when(studyRepository.findAllStudyByConditions(conditions, StudySortBy.ALL, pageable))
                .thenReturn(studies);
        when(studyRepository.countStudyByConditions(conditions, StudySortBy.ALL))
                .thenReturn(2L);

        // when
        StudyPreviewDTO result = studyQueryService.findStudiesByConditions(
                 PageRequest.of(0, 10), request, StudySortBy.ALL);

        // then
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

    }

    @Test
    @DisplayName("검색 조건 있는 스터디 검색 - 조회된 스터디가 없을 경우")
    void 검색_조건_있는_스터디_검색_시_스터디가_없는_경우() {
        // given
        SearchRequestStudyDTO request = getSearchRequestStudyDTO();
        Map<String, Object> conditions = getStringObjectMap();
        when(studyRepository.findAllStudyByConditions(conditions, StudySortBy.ALL, pageable)).thenReturn(List.of());
        when(studyRepository.countStudyByConditions(conditions, StudySortBy.ALL)).thenReturn(0L);

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findStudiesByConditions(pageable, request, StudySortBy.ALL);
        });
    }

    /* -------------------------------------------------------- 추천 스터디 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("추천 스터디 조회 - 추천 스터디가 있는 경우")
    void findRecommendStudies() {
        // given

        // Mock the memberThemeRepository to return a list of MemberTheme
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));

        when(studyThemeRepository.findAllByTheme(theme1)).thenReturn(List.of(studyTheme1));
        when(studyThemeRepository.findAllByTheme(theme2)).thenReturn(List.of(studyTheme2));

        // Mocking the studyRepository to return studies based on the study themes
        when(studyRepository.findByStudyThemeAndNotInIds(anyList(), anyList())).thenReturn(List.of(study1, study2));

        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // when
        StudyPreviewDTO result = studyQueryService.findRecommendStudies(member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getSize());  // Assuming StudyPreviewDTO has a getStudies method
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
        verify(studyRepository).findByStudyThemeAndNotInIds(anyList(), anyList());
    }


    @Test
    @DisplayName("추천 스터디 조회 - 추천 스터디가 없는 경우")
    void findRecommendStudiesOnFail() {
        // given

        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));


        when(studyThemeRepository.findAllByTheme(theme1)).thenReturn(List.of(studyTheme1));
        when(studyThemeRepository.findAllByTheme(theme2)).thenReturn(List.of(studyTheme2));

        // Mocking the studyRepository to return studies based on the study themes
        when(studyRepository.findByStudyThemeAndNotInIds(anyList(), anyList())).thenReturn(List.of());

        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findRecommendStudies(member.getId());
        });
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
        verify(studyRepository).findByStudyThemeAndNotInIds(anyList(), anyList());
    }

    @Test
    @DisplayName("추천 스터디 조회 - 회원의 관심 테마에_해당하는_스터디가 없는 경우")
    void 추천_스터디_조회_시_회원의_관심_테마에_해당하는_스터디가_없는_경우() {
        // given
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));

        when(studyThemeRepository.findAllByTheme(theme1)).thenReturn(List.of());
        when(studyThemeRepository.findAllByTheme(theme2)).thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findRecommendStudies(member.getId());
        });
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
    }

    // 유효하지 않은 사용자인 경우
    @Test
    @DisplayName("추천 스터디 조회 - 유효하지 않은 사용자인 경우")
    void findRecommendStudiesOnInvalidUser() {
        // given
        Member member = getMember();

        // when & then
        when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findRecommendStudies(member.getId());
        });
    }

    // 사용자의 관심사가 없는 경우
    @Test
    @DisplayName("추천 스터디 조회 - 사용자의 관심사가 없는 경우")
    void findRecommendStudiesOnNoInterest() {
        // given
        Member member = getMember();
        Long memberId = member.getId();

        // Mock the memberThemeRepository to return an empty list
        when(memberThemeRepository.findAllByMemberId(memberId)).thenReturn(List.of());

        // when & then
        assertThrows(MemberHandler.class, () -> {
            studyQueryService.findRecommendStudies(memberId);
        });
    }

    /* -------------------------------------------------------- 관심 Best 스터디 조회 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("관심 Best 스터디 조회 - 성공")
    void 관심_BEST_스터디_조회_성공(){
        // given
        when(studyRepository.findAllStudyByConditions(any(), any(), any()))
            .thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestedStudies(member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("관심 Best 스터디 조회 - 추천 스터디가 조회되지 않는 경우 ")
    void 관심_BEST_스터디_조회_시_추천_스터디가_없는_경우(){
        // given
        when(studyRepository.findAllStudyByConditions(any(), any(), any()))
                .thenReturn(List.of());
        // when & then
        assertThrows(StudyHandler.class, () ->
                studyQueryService.findInterestedStudies(member.getId()));
    }

    /* -------------------------------------------------------- 내 관심사 스터디 조회 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 내 전체 관심사와 검색 조건에 해당하는 스터디가 없는 경우")
    void findInterestStudiesByConditionsAllOnFail() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;

        List<Long> studyIds = List.of();

        Map<String, Object> searchConditions = getStringObjectMap();

        when(memberThemeRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(theme1))
            .thenReturn(List.of(studyTheme1));
        when(studyThemeRepository.findAllByTheme(theme2))
            .thenReturn(List.of(studyTheme2));

        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, List.of(studyTheme1, studyTheme2), sortBy, studyIds))
            .thenReturn(2L);

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(studyTheme1, studyTheme2), studyIds))
            .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestStudiesByConditionsAll(pageable, member.getId(), request, sortBy);
        });

        // then
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
    }

    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 내 전체 관심사와 검색 조건에 해당하는 스터디가 없는 경우")
    void 전체_테마_스터디_조회_시_테마에_해당하는_스터디가_없는_경우() {
        // given
        StudySortBy sortBy = StudySortBy.ALL;

        when(memberThemeRepository.findAllByMemberId(member.getId()))
                .thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(theme1))
                .thenReturn(List.of());
        when(studyThemeRepository.findAllByTheme(theme2))
                .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestStudiesByConditionsAll(pageable, member.getId(), request, sortBy);
        });

        // then
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
    }


    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 내 전체 관심사에 해당하는 스터디가 있는 경우")
    void findInterestStudiesByConditionsAll() {
        // given

        List<Long> studyIds = List.of();
        StudySortBy sortBy = StudySortBy.ALL;

        // Mock conditions
        Map<String, Object> searchConditions = getStringObjectMap();

        when(memberThemeRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(theme1))
            .thenReturn(List.of(studyTheme1));
        when(studyThemeRepository.findAllByTheme(theme2))
            .thenReturn(List.of(studyTheme2));

        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, List.of(studyTheme1, studyTheme2), sortBy, studyIds))
            .thenReturn(2L);

        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(studyTheme1, studyTheme2), studyIds))
            .thenReturn(List.of(study1, study2));

        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsAll(pageable, member.getId(), request, sortBy);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());  // Assuming StudyPreviewDTO has a getSize method
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(1)).findAllByTheme(theme1);
        verify(studyThemeRepository, times(1)).findAllByTheme(theme2);
        verify(studyRepository).countStudyByConditionsAndThemeTypesAndNotInIds(searchConditions, List.of(studyTheme1, studyTheme2), sortBy, studyIds);
        verify(studyRepository).findStudyByConditionsAndThemeTypesAndNotInIds(searchConditions, sortBy, pageable, List.of(studyTheme1, studyTheme2), studyIds);

    }

    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 페이징 테스트")
    void shouldReturnPagedStudies(){
        //given
        List<Study> studies = List.of(study1, study2);

        when(memberThemeRepository.findAllByMemberId(any())).thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(studies);
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(memberRepository.existsById(any())).thenReturn(true);


        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsAll(
            PageRequest.of(0, 10), 1L, getSearchRequestStudyDTO(), StudySortBy.ALL);

        // then
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

    }

    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 검색 조건에 따른 스터디 필터링 테스트")
    void shouldFilterStudiesBasedOnSearchConditions(){
        // given


        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(any())).thenReturn(List.of(studyTheme1, studyTheme2));
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(1L);
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1));

        // when
        // 검색 조건이 안맞는 경우, 검색 조건에 맞는 스터디가 조회 되면 안됨.
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsAll(
            pageable, member.getId(), getSearchRequestStudyDTO(), StudySortBy.ALL);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());

    }

    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(조회수 순)")
    void shouldFilterStudiesBasedOnSortConditionsByHit(){
        // given

        StudySortBy sortBy = StudySortBy.HIT;

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(any())).thenReturn(List.of(studyTheme1, studyTheme2));
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsAll(
            pageable, member.getId(), getSearchRequestStudyDTO(), sortBy);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study2.getTitle(), result.getContent().get(1).getTitle());

    }

    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(좋아요 순)")
    void shouldFilterStudiesBasedOnSortConditionsByLiked(){
        // given
        StudySortBy sortBy = StudySortBy.LIKED;

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(any())).thenReturn(List.of(studyTheme1, studyTheme2));
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study2, study1));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsAll(
            pageable, member.getId(), getSearchRequestStudyDTO(), sortBy);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(study2.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study1.getTitle(), result.getContent().get(1).getTitle());

    }

    @Test
    @DisplayName("내 전체 관심사 스터디 조회 - 회원의 관심 분야가 없는 경우")
    void findInterestStudiesByConditionsAllOnNoInterest() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of());

        // when & then
        assertThrows(MemberHandler.class, () -> {
            studyQueryService.findInterestStudiesByConditionsAll(pageable, member.getId(), request, sortBy);
        });
    }


    /* -------------------------------------------------------- 내 특정 관심사 스터디 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 내 특정 관심사에 해당하는 스터디가 있는 경우")
    void findInterestStudiesByConditionsSpecific() {
        // given

        ThemeType themeType = ThemeType.어학;
        StudySortBy sortBy = StudySortBy.ALL;
        List<Long> studyIds = List.of();

        // Mock conditions
        Map<String, Object> searchConditions = getStringObjectMap();

        when(memberThemeRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(memberTheme1));
        when(studyThemeRepository.findAllByTheme(theme1))
            .thenReturn(List.of(studyTheme1));

        // Only studyTheme1 should match
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, List.of(studyTheme1), sortBy, studyIds))
            .thenReturn(1L);
        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // Adjusting the mock to match the specific test data
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(studyTheme1), studyIds))
            .thenReturn(List.of(study1));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsSpecific(pageable, member.getId(), request, themeType, sortBy);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());  // Verify the count matches expected result
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository).findAllByTheme(theme1);  // Ensure the correct theme is queried
        verify(studyRepository).countStudyByConditionsAndThemeTypesAndNotInIds(searchConditions, List.of(studyTheme1), sortBy, studyIds);
        verify(studyRepository).findStudyByConditionsAndThemeTypesAndNotInIds(searchConditions, sortBy, pageable, List.of(studyTheme1),studyIds);
    }

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 내 특정 관심사와 검색 조건에 해당하는 스터디가 없는 경우")
    void 특정_테마_스터디_조회_시_테마에_해당하는_스터디가_없는_경우() {
        // given
        StudySortBy sortBy = StudySortBy.ALL;

        when(memberThemeRepository.findAllByMemberId(member.getId()))
                .thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(theme1))
                .thenReturn(List.of());
        when(studyThemeRepository.findAllByTheme(theme2))
                .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestStudiesByConditionsSpecific(pageable, member.getId(), request, ThemeType.어학, sortBy);
        });

        // then
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository, times(2)).findAllByTheme(any());
    }

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 내 특정 관심사에 해당하는 스터디가 없는 경우")
    void findInterestStudiesByConditionsSpecificOnFail() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;
        ThemeType themeType = ThemeType.어학;

        List<Long> studyIds = List.of();

        // Mock conditions
        Map<String, Object> searchConditions = getStringObjectMap();

        when(memberThemeRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(memberTheme1));
        when(studyThemeRepository.findAllByTheme(theme1))
            .thenReturn(List.of(studyTheme1));

        // Only studyTheme1 should match
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, List.of(studyTheme1), sortBy, studyIds))
            .thenReturn(0L);

        // Adjusting the mock to match the specific test data
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(studyTheme1), studyIds))
            .thenReturn(List.of());

        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestStudiesByConditionsSpecific(pageable, member.getId(), request, themeType, sortBy);
        });
        verify(memberThemeRepository).findAllByMemberId(member.getId());
        verify(studyThemeRepository).findAllByTheme(theme1);  // Ensure the correct theme is queried
        verify(studyRepository).countStudyByConditionsAndThemeTypesAndNotInIds(searchConditions, List.of(studyTheme1), sortBy, studyIds);
        verify(studyRepository).findStudyByConditionsAndThemeTypesAndNotInIds(searchConditions, sortBy, pageable, List.of(studyTheme1),studyIds);
    }

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 페이징 테스트")
    void shouldReturnPagedStudiesInSpecificTheme(){
        //given
        List<Study> studies = List.of(study1, study3);


        when(memberThemeRepository.findAllByMemberId(any())).thenReturn(List.of(memberTheme1));
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(studies);
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(memberRepository.existsById(any())).thenReturn(true);


        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsSpecific(
            PageRequest.of(0, 10), 1L, getSearchRequestStudyDTO(), ThemeType.어학, StudySortBy.ALL);

        // then
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

    }

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 검색 조건에 따른 스터디 필터링 테스트")
    void shouldFilterStudiesBasedOnSearchConditionsInSpecificTheme(){
        // given

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1));
        when(studyThemeRepository.findAllByTheme(any())).thenReturn(List.of(studyTheme1));
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1, study3));

        // when
        // 검색 조건이 안맞는 경우, 검색 조건에 맞는 스터디가 조회 되면 안됨.
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsSpecific(
            pageable, member.getId(), getSearchRequestStudyDTO(), ThemeType.어학, StudySortBy.ALL);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study3.getTitle(), result.getContent().get(1).getTitle());

    }

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(조회수 순)")
    void shouldFilterStudiesInSpecificThemeBasedOnSortConditionsByHit(){
        // given
        StudySortBy sortBy = StudySortBy.HIT;


        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(any())).thenReturn(List.of(studyTheme1, studyTheme2));
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(3L);
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1, study3, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsSpecific(
            pageable, member.getId(), getSearchRequestStudyDTO(), ThemeType.어학, sortBy);

        // then
        assertEquals(3, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study3.getTitle(), result.getContent().get(1).getTitle());
        assertEquals(study2.getTitle(), result.getContent().get(2).getTitle());

    }

    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(좋아요 순)")
    void shouldFilterStudiesInSpecificThemeBasedOnSortConditionsByLiked(){
        // given
        StudySortBy sortBy = StudySortBy.LIKED;


        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of(memberTheme1, memberTheme2));
        when(studyThemeRepository.findAllByTheme(any())).thenReturn(List.of(studyTheme1, studyTheme2));
        when(studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(3L);
        when(studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study2, study1, study3));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestStudiesByConditionsAll(
            pageable, member.getId(), getSearchRequestStudyDTO(), sortBy);

        // then
        assertEquals(3, result.getTotalElements());
        assertEquals(study2.getTitle(), result.getContent().get(0).getTitle());
    }




    // 만약 회원의 관심사에 해당하는 테마가 없다면 예외 처리 -> 입력한 테마와 회원이 등록한 테마가 다르면 에러 발생
    @Test
    @DisplayName("내 특정 관심사 스터디 조회 - 회원의 관심사에 해당하는 테마가 없는 경우")
    void noThemeInMemberInterest() {
        // given
        ThemeType themeType = ThemeType.어학;
        StudySortBy sortBy = StudySortBy.ALL;

        SearchRequestStudyDTO request = getSearchRequestStudyDTO();

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(memberThemeRepository.findAllByMemberId(member.getId())).thenReturn(List.of());

        // when & then
        assertThrows(MemberHandler.class, () -> {
            studyQueryService.findInterestStudiesByConditionsSpecific(pageable, member.getId(), request, themeType, sortBy);
        });
    }

    /* -------------------------------------------------------- 내 전체 관심 지역 스터디 조회 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 내 전체 관심 지역에 해당하는 스터디가 있는 경우")
    void findInterestRegionStudiesByConditionsAll() {
        // given
        StudySortBy sortBy = StudySortBy.ALL;

        List<Long> studyIds = List.of();

        // Mock conditions
        Map<String, Object> searchConditions = getStringObjectMap();

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(region1))
            .thenReturn(List.of(regionStudy1));
        when(regionStudyRepository.findAllByRegion(region2))
            .thenReturn(List.of(regionStudy2));

        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, List.of(regionStudy1, regionStudy2), sortBy, studyIds))
            .thenReturn(2L);

        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(regionStudy1, regionStudy2), studyIds))
            .thenReturn(List.of(study1, study2));

        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsAll(pageable, member.getId(), request, sortBy);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());  // Assuming StudyPreviewDTO has a getSize method
        verify(preferredRegionRepository).findAllByMemberId(member.getId());
        verify(regionStudyRepository, times(1)).findAllByRegion(region1);
        verify(regionStudyRepository, times(1)).findAllByRegion(region2);
        verify(studyRepository).countStudyByConditionsAndRegionStudiesAndNotInIds(searchConditions, List.of(regionStudy1, regionStudy2), sortBy, studyIds);
        verify(studyRepository).findStudyByConditionsAndRegionStudiesAndNotInIds(searchConditions, sortBy, pageable, List.of(regionStudy1, regionStudy2), studyIds);
    }

    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 내 전체 관심 지역에 해당하는 스터디가 없는 경우")
    void 전체_관심_지역_스터디_조회_시_해당_지역에_스터디가_없는_경우() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
                .thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of( ));

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestRegionStudiesByConditionsAll(pageable, member.getId(), request, sortBy);
        });

        // then
        verify(regionStudyRepository, times(1)).findAllByRegion(region1);
    }


    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 내 전체 관심 지역에 해당하는 스터디가 없는 경우")
    void findInterestRegionStudiesByConditionsAllOnFail() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;

        List<Long> studyIds = List.of();

        Map<String, Object> searchConditions = getStringObjectMap();

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1, regionStudy2));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, List.of(regionStudy1, regionStudy2), sortBy, studyIds))
            .thenReturn(2L);

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(regionStudy1, regionStudy2), studyIds))
            .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestRegionStudiesByConditionsAll(pageable, member.getId(), request, sortBy);
        });

        // then
        verify(regionStudyRepository, times(1)).findAllByRegion(region1);
        verify(regionStudyRepository, times(1)).findAllByRegion(region1);
    }



    @Test
    @DisplayName("내 전체 관심 지역  스터디 조회 - 페이징 테스트")
    void shouldReturnPagedStudiesByRegion(){
        //given
        List<Study> studies = List.of(study1, study2);

        when(preferredRegionRepository.findAllByMemberId(any())).thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1, regionStudy2));
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(studies);
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(memberRepository.existsById(any())).thenReturn(true);


        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsAll(
            PageRequest.of(0, 10), 1L, getSearchRequestStudyDTO(), StudySortBy.ALL);

        // then
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

    }

    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 검색 조건에 따른 스터디 필터링 테스트")
    void shouldFilterStudiesBasedOnSearchConditionsByRegion(){
        // given


        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1, regionStudy2));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(1L);
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1));

        // when
        // 검색 조건이 안맞는 경우, 검색 조건에 맞는 스터디가 조회 되면 안됨.
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsAll(
            pageable, member.getId(), getSearchRequestStudyDTO(), StudySortBy.ALL);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());

    }

    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(조회수 순)")
    void shouldFilterRegionStudiesBasedOnSortConditionsByHit(){
        // given

        StudySortBy sortBy = StudySortBy.HIT;

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1, regionStudy2));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsAll(
            pageable, member.getId(), getSearchRequestStudyDTO(), sortBy);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study2.getTitle(), result.getContent().get(1).getTitle());

    }

    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(좋아요 순)")
    void shouldFilterRegionStudiesBasedOnSortConditionsByLiked(){
        // given
        StudySortBy sortBy = StudySortBy.LIKED;

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1, regionStudy2));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study2, study1));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsAll(
            pageable, member.getId(), getSearchRequestStudyDTO(), sortBy);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(study2.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study1.getTitle(), result.getContent().get(1).getTitle());

    }

    @Test
    @DisplayName("내 전체 관심 지역 스터디 조회 - 회원의 관심 지역 없는 경우")
    void findInterestRegionStudiesByConditionsAllOnNoInterest() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of());

        // when & then
        assertThrows(MemberHandler.class, () -> {
            studyQueryService.findInterestRegionStudiesByConditionsAll(pageable, member.getId(), request, sortBy);
        });
    }


    /* -------------------------------------------------------- 내 특정 관심 지역 스터디 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 내 특정 관심 지역에 해당하는 스터디가 있는 경우")
    void findInterestRegionStudiesByConditionsSpecific() {
        // given

        String regionCode = "4159034000";
        StudySortBy sortBy = StudySortBy.ALL;
        List<Long> studyIds = List.of();

        SearchRequestStudyDTO request = getSearchRequestStudyDTO();

        // Mock conditions
        Map<String, Object> searchConditions = getStringObjectMap();

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(preferredRegion1));
        when(regionStudyRepository.findAllByRegion(region1))
            .thenReturn(List.of(regionStudy1));

        // Only studyTheme1 should match
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, List.of(regionStudy1), sortBy, studyIds))
            .thenReturn(1L);

        // Adjusting the mock to match the specific test data
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(regionStudy1), studyIds))
            .thenReturn(List.of(study1));

        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsSpecific(pageable, member.getId(), request, regionCode, sortBy);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());  // Verify the count matches expected result
        verify(preferredRegionRepository).findAllByMemberId(member.getId());
        verify(regionStudyRepository).findAllByRegion(region1);  // Ensure the correct theme is queried
        verify(studyRepository).countStudyByConditionsAndRegionStudiesAndNotInIds(searchConditions, List.of(regionStudy1), sortBy, studyIds);
        verify(studyRepository).findStudyByConditionsAndRegionStudiesAndNotInIds(searchConditions, sortBy, pageable, List.of(regionStudy1), studyIds);


    }

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 내 특정 관심 지역에 해당하는 스터디가 없는 경우")
    void 특정_관심_지역_스터디_조회_시_해당_지역에_스터디가_없는_경우() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
                .thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of( ));

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestRegionStudiesByConditionsSpecific(pageable, member.getId(), request, region1.getCode(), sortBy);
        });

        // then
        verify(regionStudyRepository, times(2)).findAllByRegion(any());
    }

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 내 특정 관심 지역에 해당하는 스터디가 없는 경우")
    void findInterestRegionStudiesByConditionsSpecificOnFail() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;
        String regionCode = region1.getCode();

        List<Long> studyIds = List.of();

        // Mock conditions
        Map<String, Object> searchConditions = getStringObjectMap();

        when(preferredRegionRepository.findAllByMemberId(member.getId()))
            .thenReturn(List.of(preferredRegion1));
        when(regionStudyRepository.findAllByRegion(region1))
            .thenReturn(List.of(regionStudy1));

        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, List.of(regionStudy1), sortBy, studyIds))
            .thenReturn(0L);

        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(
            searchConditions, sortBy, pageable, List.of(regionStudy1), studyIds))
            .thenReturn(List.of());

        when(memberRepository.existsById(member.getId())).thenReturn(true);

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestRegionStudiesByConditionsSpecific(pageable, member.getId(), request, regionCode, sortBy);
        });
        verify(preferredRegionRepository).findAllByMemberId(member.getId());
        verify(regionStudyRepository).findAllByRegion(region1);  // Ensure the correct theme is queried
        verify(studyRepository).countStudyByConditionsAndRegionStudiesAndNotInIds(searchConditions, List.of(regionStudy1), sortBy, studyIds);
        verify(studyRepository).findStudyByConditionsAndRegionStudiesAndNotInIds(searchConditions, sortBy, pageable, List.of(regionStudy1),studyIds);
    }

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 페이징 테스트")
    void shouldReturnPagedStudiesInSpecificRegion(){
        //given
        List<Study> studies = List.of(study1, study3);


        when(preferredRegionRepository.findAllByMemberId(any())).thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(studies);
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1, regionStudy2));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(memberRepository.existsById(any())).thenReturn(true);


        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsSpecific(
            PageRequest.of(0, 10), 1L, getSearchRequestStudyDTO(), region1.getCode(), StudySortBy.ALL);

        // then
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

    }

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 검색 조건에 따른 스터디 필터링 테스트")
    void shouldFilterStudiesBasedOnSearchConditionsInSpecificRegion(){
        // given

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of(preferredRegion1));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(2L);
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1, study3));

        // when
        // 검색 조건이 안맞는 경우, 검색 조건에 맞는 스터디가 조회 되면 안됨.
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsSpecific(
            pageable, member.getId(), getSearchRequestStudyDTO(), region1.getCode(), StudySortBy.ALL);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study3.getTitle(), result.getContent().get(1).getTitle());

    }

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(조회수 순)")
    void shouldFilterStudiesInSpecificRegionBasedOnSortConditionsByHit(){
        // given
        StudySortBy sortBy = StudySortBy.HIT;


        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of(preferredRegion1, preferredRegion2));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1, regionStudy2));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(3L);
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study1, study3, study2));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsSpecific(
            pageable, member.getId(), getSearchRequestStudyDTO(), region1.getCode(), sortBy);

        // then
        assertEquals(3, result.getTotalElements());
        assertEquals(study1.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(study3.getTitle(), result.getContent().get(1).getTitle());
        assertEquals(study2.getTitle(), result.getContent().get(2).getTitle());

    }

    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 정렬 조건에 따른 스터디 필터링 테스트(좋아요 순)")
    void shouldFilterStudiesInSpecificRegionBasedOnSortConditionsByLiked(){
        // given
        StudySortBy sortBy = StudySortBy.LIKED;


        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of(preferredRegion1));
        when(regionStudyRepository.findAllByRegion(any())).thenReturn(List.of(regionStudy1));
        when(studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any()))
            .thenReturn(3L);
        when(studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(any(), any(), any(), any(), any()))
            .thenReturn(List.of(study2, study1, study3));

        // when
        StudyPreviewDTO result = studyQueryService.findInterestRegionStudiesByConditionsSpecific(
            pageable, member.getId(), getSearchRequestStudyDTO(),region1.getCode() ,sortBy);

        // then
        assertEquals(3, result.getTotalElements());
        assertEquals(study2.getTitle(), result.getContent().get(0).getTitle());
    }




    // 만약 회원의 관심사에 해당하는 테마가 없다면 예외 처리 -> 입력한 테마와 회원이 등록한 테마가 다르면 에러 발생
    @Test
    @DisplayName("내 특정 관심 지역 스터디 조회 - 회원의 관심 지역에 해당하는 스터디가 없는 경우")
    void noThemeInMemberInterestRegion() {
        // given
        StudySortBy sortBy = StudySortBy.ALL;

        SearchRequestStudyDTO request = getSearchRequestStudyDTO();

        when(memberRepository.existsById(member.getId())).thenReturn(true);
        when(preferredRegionRepository.findAllByMemberId(member.getId())).thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findInterestRegionStudiesByConditionsSpecific(pageable, member.getId(), request, region1.getCode(), sortBy);
        });
    }


    //------------------------------------ 모집 중 스터디 조회 ------------------------------------------------------


    @Test
    @DisplayName("모집 중 스터디 조회 - 모집 중인 스터디가 있는 경우")
    void findRecruitingStudiesByConditions() {
        // given

        StudySortBy sortBy = StudySortBy.ALL;
        Map<String, Object> searchConditions = getStringObjectMap();

        when(studyRepository.findRecruitingStudyByConditions(searchConditions, sortBy, pageable))
            .thenReturn(List.of(study1, study2));
        when(studyRepository.countStudyByConditions(searchConditions, sortBy))
            .thenReturn(2L);

        // SecurityContext와 Authentication을 모킹
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("1"); // 현재 사용자 ID를 1로 가정

        SecurityContextHolder.setContext(securityContext);

        // when
        StudyPreviewDTO result = studyQueryService.findRecruitingStudiesByConditions(pageable, request, sortBy);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());  // Verify the count of elements
        verify(studyRepository).findRecruitingStudyByConditions(searchConditions, sortBy, pageable);
        verify(studyRepository).countStudyByConditions(searchConditions, sortBy);
    }

    @Test
    @DisplayName("모집 중 스터디 조회 - 조회 된 스터디가 없는 경우")
    void 조회_된_스터디가_없는_경우() {
        // given
        Map<String, Object> searchConditions = getStringObjectMap();

        when(studyRepository.findRecruitingStudyByConditions(searchConditions, StudySortBy.ALL, pageable))
                .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findRecruitingStudiesByConditions(pageable, request, StudySortBy.ALL);
        });

    }

    /* -------------------------------------------------------- 찜한 스터디 조회 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("찜한 스터디 조회 - 찜한 스터디가 있는 경우")
    void findLikedStudies() {
        // given
        Member member = getMember();

        when(preferredStudyRepository.findByMemberIdAndStudyLikeStatusOrderByCreatedAtDesc(member.getId(), StudyLikeStatus.LIKE, PageRequest.of(0, 10)))
            .thenReturn(List.of(preferredStudy1, preferredStudy2));
        when(preferredStudyRepository.countByMemberIdAndStudyLikeStatus(member.getId(), StudyLikeStatus.LIKE))
            .thenReturn(2L);

        // when
        StudyPreviewDTO result = studyQueryService.findLikedStudies(member.getId(), PageRequest.of(0, 10));

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(preferredStudyRepository).findByMemberIdAndStudyLikeStatusOrderByCreatedAtDesc(member.getId(), StudyLikeStatus.LIKE, PageRequest.of(0, 10));

    }

    @Test
    @DisplayName("찜한 스터디 조회 - 찜한 스터디가 없는 경우")
    void 찜한_스터디가_없는_경우() {
        // given
        Member member = getMember();

        when(preferredStudyRepository.findByMemberIdAndStudyLikeStatusOrderByCreatedAtDesc(member.getId(), StudyLikeStatus.LIKE, PageRequest.of(0, 10)))
                .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findLikedStudies(member.getId(), PageRequest.of(0, 10));
        });

    }


    /* -------------------------------------------------------- 키워드를 통한 스터디 검색 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("키워드로 스터디 검색 - 해당 키워드에 해당하는 스터디가 있는 경우")
    void findStudiesByKeyword() {

        // given
        String keyword = "English";
        StudySortBy sortBy = StudySortBy.ALL;

        when(studyRepository.findAllByTitleContaining(keyword, sortBy, pageable))
            .thenReturn(List.of(study1));
        when(studyRepository.countAllByTitleContaining(keyword, sortBy))
            .thenReturn(1L);

        // SecurityContext와 Authentication을 모킹
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("1"); // 현재 사용자 ID를 1로 가정

        SecurityContextHolder.setContext(securityContext);

        // when
        StudyPreviewDTO result = studyQueryService.findStudiesByKeyword(pageable, keyword, sortBy);



        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(studyRepository).findAllByTitleContaining(keyword, sortBy, pageable);
    }

    @Test
    @DisplayName("키워드로 스터디 검색 - 해당 키워드에 해당하는 스터디가 없는 경우")
    void 키워드_스터디_검색_시_조회_된_스터디가_없는_경우() {
        // given
        String keyword = "English";
        StudySortBy sortBy = StudySortBy.ALL;

        when(studyRepository.findAllByTitleContaining(keyword, sortBy, pageable))
                .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> {
            studyQueryService.findStudiesByKeyword(pageable, keyword, sortBy);
        });

    }
    /* -------------------------------------------------------- 테마 별 스터디 검색 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("테마 별 스터디 검색 - 해당 테마에 해당하는 스터디가 있는 경우")
    void findStudiesByTheme() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ThemeType themeType = ThemeType.어학;
        StudySortBy sortBy = StudySortBy.ALL;
        Theme theme = getTheme(1L, themeType);
        StudyTheme studyTheme = new StudyTheme(theme, study1);

        when(themeRepository.findByStudyTheme(themeType)).thenReturn(Optional.ofNullable(theme));
        when(studyThemeRepository.findAllByTheme(theme)).thenReturn(List.of(studyTheme));

        when(studyRepository.findByStudyTheme(List.of(studyTheme), sortBy, pageable))
            .thenReturn(List.of(study1));
        when(studyRepository.countStudyByStudyTheme(List.of(studyTheme), sortBy))
            .thenReturn(1L);
        // SecurityContext와 Authentication을 모킹
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("1"); // 현재 사용자 ID를 1로 가정

        SecurityContextHolder.setContext(securityContext);

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

    /* -------------------------------------------------------- 내가 참여하는 스터디 조회 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("내가 참여하고 있는 스터디 조회 - 참여하고 있는 스터디가 있는 경우")
    void findOngoingStudiesByMemberId() {

        // given

        when(memberStudyRepository.findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPROVED))
            .thenReturn(List.of(memberStudy1, memberStudy2));
        when(studyRepository.findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable))
            .thenReturn(List.of(study1, study2));
        when(memberStudyRepository.countByMemberIdAndStatus(member.getId(), ApplicationStatus.APPROVED))
            .thenReturn(2L);

        // when
        StudyPreviewDTO result = studyQueryService.findOngoingStudiesByMemberId(pageable, member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(memberStudyRepository).findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPROVED);
        verify(studyRepository).findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable);

    }

    /* -------------------------------------------------------- 내가 신청한 스터디 조회 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("내가 신청한 스터디 조회 - 신청한 스터디가 있는 경우")
    void findAppliedStudies() {
        // given

        when(memberStudyRepository.findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPLIED))
            .thenReturn(List.of(memberStudy1, memberStudy2));
        when(studyRepository.findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable))
            .thenReturn(List.of(study1, study2));
        when(memberStudyRepository.countByMemberIdAndStatus(member.getId(), ApplicationStatus.APPLIED))
            .thenReturn(2L);

        // when
        StudyPreviewDTO result = studyQueryService.findAppliedStudies(pageable, member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(memberStudyRepository).findAllByMemberIdAndStatus(member.getId(), ApplicationStatus.APPLIED);
        verify(studyRepository).findByMemberStudy(List.of(memberStudy1, memberStudy2), pageable);

    }
    /* -------------------------------------------------------- 내가 모집 중인 스터디 조회 ------------------------------------------------------------------------*/
    @Test
    @DisplayName("내가 모집중인 스터디 조회 - 모집중인 스터디가 있는 경우")
    void findMyRecruitingStudies() {
        // given
        Member member = getMember();

        Pageable pageable = PageRequest.of(0, 10);


        when(memberStudyRepository.findAllByMemberIdAndIsOwned(member.getId(), true))
            .thenReturn(List.of(memberStudy1, memberStudy2));
        when(studyRepository.findRecruitingStudiesByMemberStudy(List.of(memberStudy1, memberStudy2), pageable))
            .thenReturn(List.of(study1, study2));
        when(memberStudyRepository.countByMemberIdAndIsOwned(member.getId(), true))
            .thenReturn(2L);

        // when
        StudyPreviewDTO result = studyQueryService.findMyRecruitingStudies(pageable, member.getId());

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(memberStudyRepository).findAllByMemberIdAndIsOwned(member.getId(), true);
        verify(studyRepository).findRecruitingStudiesByMemberStudy(List.of(memberStudy1, memberStudy2), pageable);

    }

    /*-------------------------------------------------------- Utils ------------------------------------------------------------------------*/


    private static Member getMember() {
        return Member.builder()
            .id(1L)
            .build();
    }
    private static void initStudy() {
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
            .minAge(25)
            .maxAge(30)
            .fee(2000)
            .profileImage("profile2.jpg")
            .hasFee(true)
            .isOnline(false)
            .goal("Win a competition")
            .introduction("This is a competition study group")
            .title("Competition Study Group")
            .maxPeople(15L)
            .build();
        study3 = Study.builder()
                .gender(Gender.MALE)
                .minAge(18)
                .maxAge(35)
                .fee(1000)
                .profileImage("profile1.jpg")
                .hasFee(true)
                .isOnline(true)
                .goal("Learn Korean")
                .introduction("This is an Korean study group")
                .title("Korean Study Group")
                .maxPeople(10L)
                .build();

        study1.increaseHit(); study1.increaseHit();
        study3.increaseHit();
        study2.addPreferredStudy(getPreferredStudy(getMember(), study2));
    }

    private static Theme getTheme(Long id, ThemeType themeType) {
        return Theme.builder()
            .id(id)
            .studyTheme(themeType)
            .build();
    }


    private static Region getRegion(String neighborhood, String number) {
        return Region.builder()
            .province("경기도")
            .neighborhood(neighborhood)
            .district("화성시")
            .code(number)
            .build();
    }

    private static PreferredStudy getPreferredStudy(Member member, Study study) {
        return PreferredStudy.builder()
            .member(member)
            .study(study)
            .studyLikeStatus(StudyLikeStatus.LIKE)
            .build();
    }

    private static MemberStudy getMemberStudy(Member member, Study study) {
        return MemberStudy.builder()
            .member(member)
            .study(study)
                .isOwned(true)
                .status(ApplicationStatus.APPROVED)
            .build();
    }

    private static Map<String, Object> getStringObjectMap() {
        // Mock conditions
        Map<String, Object> searchConditions = new HashMap<>();
        searchConditions.put("gender", Gender.MALE);
        searchConditions.put("minAge", 20);
        searchConditions.put("maxAge", 40);
        searchConditions.put("isOnline", true);
        searchConditions.put("hasFee", true);
        searchConditions.put("fee", 10000);
        return searchConditions;
    }

    private static SearchRequestStudyDTO getSearchRequestStudyDTO() {
        return SearchRequestStudyDTO.builder()
            .gender(Gender.MALE)
            .minAge(20)
            .maxAge(40)
            .fee(10000)
            .isOnline(true)
            .hasFee(true)
            .build();
    }

}

package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.StudySortBy;
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
import com.example.spot.repository.RegionRepository;
import com.example.spot.repository.RegionStudyRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.repository.StudyThemeRepository;
import com.example.spot.repository.ThemeRepository;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.SearchStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyQueryServiceImpl implements StudyQueryService {

    // 스터디 관련 조회
    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final PreferredStudyRepository preferredStudyRepository;

    // 관심사 관련 조회
    private final ThemeRepository themeRepository;
    private final StudyThemeRepository studyThemeRepository;
    private final MemberThemeRepository memberThemeRepository;

    // 지역 관련 조회
    private final RegionRepository regionRepository;
    private final PreferredRegionRepository preferredRegionRepository;
    private final RegionStudyRepository regionStudyRepository;

    @Override
    public StudyPreviewDTO findRecommendStudies(Long memberId) {

        // MemberId로 회원 관심사 전체 조회
        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId).stream()
            .map(MemberTheme::getTheme)
            .toList();

        // 회원 관심사로 스터디 테마 조회
        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(theme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();
        List<Study> studies = studyRepository.findByStudyTheme(studyThemes);

        return getDTOs(studies, Pageable.unpaged(), studies.size());
    }

    @Override
    public StudyPreviewDTO findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchRequestStudyDTO request, StudySortBy sortBy) {

        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId).stream()
            .map(MemberTheme::getTheme)
            .toList();

        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(theme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();

        Map<String, Object> conditions = getSearchConditions(request);

        long totalElements = studyRepository.countStudyByConditionsAndThemeTypes(conditions, studyThemes, sortBy);


        List<Study> studies = studyRepository.findStudyByConditionsAndThemeTypes(
            conditions, sortBy,
            pageable, studyThemes);

        return getDTOs(studies, pageable, totalElements);
    }


    @Override
    public StudyPreviewDTO findInterestStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchRequestStudyDTO request, ThemeType themeType, StudySortBy sortBy) {

        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId)
            .stream()
            .map(MemberTheme::getTheme)
            .collect(Collectors.toList());

        if (themes.stream().noneMatch(theme -> theme.getStudyTheme().equals(themeType))) {
            throw new StudyHandler(ErrorStatus._BAD_REQUEST);
        }

        Theme theme = findThemeByType(themes, themeType);

        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(studytheme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();

        Map<String, Object> conditions = getSearchConditions(request);

        long totalElements = studyRepository.countStudyByConditionsAndThemeTypes(conditions, studyThemes, sortBy);

        List<Study> studies = studyRepository.findStudyByConditionsAndThemeTypes(
            conditions, sortBy, pageable, studyThemes);

        return getDTOs(studies, pageable, totalElements);
    }


    @Override
    public StudyPreviewDTO findInterestRegionStudiesByConditionsAll(Pageable pageable,
        Long memberId, SearchRequestStudyDTO request, StudySortBy sortBy) {

        List<Region> regions = preferredRegionRepository.findAllByMemberId(memberId).stream()
            .map(PreferredRegion::getRegion)
            .toList();

        List<RegionStudy> regionStudies = regions.stream()
            .flatMap(region -> regionStudyRepository.findAllByRegion(region).stream())
            .toList();

        Map<String, Object> conditions = getSearchConditions(request);

        long totalElements = studyRepository.countStudyByConditionsAndRegionStudies(conditions, regionStudies, sortBy);


        List<Study> studies = studyRepository.findStudyByConditionsAndRegionStudies(
            conditions, sortBy,
            pageable, regionStudies);

        return getDTOs(studies, pageable, totalElements);
    }

    @Override
    public StudyPreviewDTO findInterestRegionStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchRequestStudyDTO request, String regionCode, StudySortBy sortBy) {

        List<Region> regions = preferredRegionRepository.findAllByMemberId(memberId)
            .stream()
            .map(PreferredRegion::getRegion)
            .toList();

        if (regions.stream().noneMatch(region -> region.getCode().equals(regionCode)))
            throw new StudyHandler(ErrorStatus._BAD_REQUEST);

        Region region = findRegionByCode(regions, regionCode);

        List<RegionStudy> regionStudies = regions.stream()
            .flatMap(regionStudy -> regionStudyRepository.findAllByRegion(region).stream())
            .toList();

        Map<String, Object> conditions = getSearchConditions(request);

        long totalElements = studyRepository.countStudyByConditionsAndRegionStudies(conditions, regionStudies, sortBy);

        List<Study> studies = studyRepository.findStudyByConditionsAndRegionStudies(
            conditions, sortBy, pageable, regionStudies);

        return getDTOs(studies, pageable, totalElements);
    }

    @Override
    public StudyPreviewDTO findRecruitingStudiesByConditions(Pageable pageable,
        SearchRequestStudyDTO request, StudySortBy sortBy) {

        Map<String, Object> conditions = getSearchConditions(request);
        List<Study> studies = studyRepository.findStudyByConditions(conditions,
            sortBy, pageable);

        long totalElements = studyRepository.countStudyByConditions(conditions, sortBy);

        return getDTOs(studies, pageable, totalElements);
    }

    @Override
    public StudyPreviewDTO findLikedStudies(Long memberId) {
        List<PreferredStudy> preferredStudyList = preferredStudyRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        List<Study> studies = preferredStudyList.stream()
            .map(PreferredStudy::getStudy)
            .toList();

        return getDTOs(studies, Pageable.unpaged(), studies.size());
    }

    @Override
    public StudyPreviewDTO findStudiesByKeyword(Pageable pageable,
        String keyword, StudySortBy sortBy) {
        List<Study> studies = studyRepository.findAllByTitleContaining(keyword, sortBy, pageable);

        long totalElements = studyRepository.countAllByTitleContaining(keyword, sortBy);
        return getDTOs(studies, pageable, totalElements);
    }

    @Override
    public StudyPreviewDTO findStudiesByTheme(Pageable pageable, ThemeType theme, StudySortBy sortBy) {
        Theme themeEntity = themeRepository.findByStudyTheme(theme)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));

        List<StudyTheme> studyThemes = studyThemeRepository.findAllByTheme(themeEntity);

        List<Study> studies = studyRepository.findByStudyTheme(studyThemes, sortBy, pageable);


        long totalElements = studyRepository.countStudyByStudyTheme(studyThemes, sortBy);
        return getDTOs(studies, pageable, totalElements);
    }

    @Override
    public StudyPreviewDTO findOngoingStudiesByMemberId(Pageable pageable, Long memberId) {
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(
            memberId, ApplicationStatus.ONGOING);
        List<Study> studies = studyRepository.findByMemberStudy(memberStudies, pageable);

        return getDTOs(studies, pageable, studies.size());
    }

    @Override
    public StudyPreviewDTO findAppliedStudies(Pageable pageable, Long memberId) {
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(
            memberId, ApplicationStatus.APPLIED);
        List<Study> studies = studyRepository.findByMemberStudy(memberStudies, pageable);
        return getDTOs(studies, pageable, studies.size());
    }

    @Override
    public StudyPreviewDTO findMyRecruitingStudies(Pageable pageable, Long memberId) {
        return null;
    }

    private static Map<String, Object> getSearchConditions(SearchRequestStudyDTO request) {
        // 검색 조건 맵 생성
        Map<String, Object> search = new HashMap<>();
        if (request.getGender() != null)
            search.put("gender", request.getGender());
        if (request.getMinAge() != null)
            search.put("minAge", request.getMinAge());
        if (request.getMaxAge() != null)
            search.put("maxAge", request.getMaxAge());
        if (request.getIsOnline() != null)
            search.put("isOnline", request.getIsOnline());
        if (request.getHasFee() != null)
            search.put("hasFee", request.getHasFee());
        if (request.getFee() != null)
            search.put("fee", request.getFee());
        return search;
    }

    private static SearchResponseDTO.StudyPreviewDTO getDTOs(List<Study> studies, Pageable pageable, long totalElements) {
        List<SearchResponseDTO.SearchStudyDTO> stream = studies.stream()
            .map(SearchResponseDTO.SearchStudyDTO::new)
            .toList();
        Page<SearchResponseDTO.SearchStudyDTO> page = new PageImpl<>(stream, pageable, totalElements);
        return new StudyPreviewDTO(page, stream, totalElements);
    }


    private Theme findThemeByType(List<Theme> themes, ThemeType themeType) {
        return themes.stream()
            .filter(t -> t.getStudyTheme().equals(themeType))
            .findFirst()
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));
    }

    private Region findRegionByCode(List<Region> regions, String regionCode) {
        return regions.stream()
            .filter(r -> r.getCode().equals(regionCode))
            .findFirst()
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));
    }

}

package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
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
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.MemberThemeRepository;
import com.example.spot.repository.PreferredRegionRepository;
import com.example.spot.repository.PreferredStudyRepository;
import com.example.spot.repository.RegionRepository;
import com.example.spot.repository.RegionStudyRepository;
import com.example.spot.repository.ScheduleRepository;
import com.example.spot.repository.StudyPostRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.repository.StudyThemeRepository;
import com.example.spot.repository.ThemeRepository;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.MyPageDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.SearchStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO.StudyMemberDTO;
import com.example.spot.web.dto.study.response.StudyPostResponseDTO;
import com.example.spot.web.dto.study.response.StudyScheduleResponseDTO;
import com.example.spot.web.dto.study.response.StudyScheduleResponseDTO.StudyScheduleDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StudyQueryServiceImpl implements StudyQueryService {

    private final MemberRepository memberRepository;

    // 스터디 관련 조회
    private final StudyRepository studyRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final PreferredStudyRepository preferredStudyRepository;

    // 관심사 관련 조회
    private final ThemeRepository themeRepository;
    private final StudyThemeRepository studyThemeRepository;
    private final MemberThemeRepository memberThemeRepository;

    // 지역 관련 조회
    private final PreferredRegionRepository preferredRegionRepository;
    private final RegionStudyRepository regionStudyRepository;

    public StudyInfoResponseDTO.StudyInfoDTO getStudyInfo(Long studyId) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        List<MemberStudy> memberStudyList = study.getMemberStudies().stream()
                .filter(MemberStudy::getIsOwned)
                .toList();

        if (memberStudyList.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_OWNER_NOT_FOUND);
        }

        Member owner = memberStudyList.get(0).getMember();
        return StudyInfoResponseDTO.StudyInfoDTO.toDTO(study, owner);
    }

    @Override
    public MyPageDTO getMyPageStudyCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));
        long appliedStudies = memberStudyRepository.countByMemberIdAndStatus(memberId, ApplicationStatus.APPLIED);
        long ongoingStudies = memberStudyRepository.countByMemberIdAndStatus(memberId, ApplicationStatus.ONGOING);
        long myRecruitingStudies = memberStudyRepository.countByMemberIdAndIsOwned(memberId, true);
        return MyPageDTO.builder()
            .name(member.getName())
            .appliedStudies(appliedStudies)
            .ongoingStudies(ongoingStudies)
            .myRecruitingStudies(myRecruitingStudies)
            .build();
    }

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

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, Pageable.unpaged(), studies.size(), memberId);
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

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
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

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
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

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
    }

    @Override
    public StudyPreviewDTO findInterestRegionStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchRequestStudyDTO request, String regionCode, StudySortBy sortBy) {

        List<Region> regions = preferredRegionRepository.findAllByMemberId(memberId)
            .stream()
            .map(PreferredRegion::getRegion)
            .toList();

        if (regions.stream().noneMatch(region -> region.getCode().equals(regionCode)))
            throw new StudyHandler(ErrorStatus._STUDY_REGION_IS_NOT_MATCH);

        Region region = findRegionByCode(regions, regionCode);

        List<RegionStudy> regionStudies = regions.stream()
            .flatMap(regionStudy -> regionStudyRepository.findAllByRegion(region).stream())
            .toList();

        Map<String, Object> conditions = getSearchConditions(request);

        long totalElements = studyRepository.countStudyByConditionsAndRegionStudies(conditions, regionStudies, sortBy);

        List<Study> studies = studyRepository.findStudyByConditionsAndRegionStudies(
            conditions, sortBy, pageable, regionStudies);

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
    }

    @Override
    public StudyPreviewDTO findRecruitingStudiesByConditions(Pageable pageable,
        SearchRequestStudyDTO request, StudySortBy sortBy) {

        Map<String, Object> conditions = getSearchConditions(request);
        List<Study> studies = studyRepository.findStudyByConditions(conditions,
            sortBy, pageable);

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        long totalElements = studyRepository.countStudyByConditions(conditions, sortBy);



        return getDTOs(studies, pageable, totalElements, null);
    }

    @Override
    public StudyPreviewDTO findLikedStudies(Long memberId) {
        List<PreferredStudy> preferredStudyList = preferredStudyRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        List<Study> studies = preferredStudyList.stream()
            .map(PreferredStudy::getStudy)
            .toList();

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, Pageable.unpaged(), studies.size(), memberId);
    }

    @Override
    public StudyPreviewDTO findStudiesByKeyword(Pageable pageable,
        String keyword, StudySortBy sortBy) {
        List<Study> studies = studyRepository.findAllByTitleContaining(keyword, sortBy, pageable);

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        long totalElements = studyRepository.countAllByTitleContaining(keyword, sortBy);
        return getDTOs(studies, pageable, totalElements, null);
    }

    @Override
    public StudyPreviewDTO findStudiesByTheme(Pageable pageable, ThemeType theme, StudySortBy sortBy) {
        Theme themeEntity = themeRepository.findByStudyTheme(theme)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));

        List<StudyTheme> studyThemes = studyThemeRepository.findAllByTheme(themeEntity);

        List<Study> studies = studyRepository.findByStudyTheme(studyThemes, sortBy, pageable);

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);


        long totalElements = studyRepository.countStudyByStudyTheme(studyThemes, sortBy);
        return getDTOs(studies, pageable, totalElements, null);
    }

    @Override
    public StudyPreviewDTO findOngoingStudiesByMemberId(Pageable pageable, Long memberId) {
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(
            memberId, ApplicationStatus.APPROVED);
        List<Study> studies = studyRepository.findByMemberStudy(memberStudies, pageable);

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, studies.size(), memberId);
    }

    @Override
    public StudyPreviewDTO findAppliedStudies(Pageable pageable, Long memberId) {
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(
            memberId, ApplicationStatus.APPLIED);
        List<Study> studies = studyRepository.findByMemberStudy(memberStudies, pageable);

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, studies.size(), memberId);
    }

    @Override
    public StudyPreviewDTO findMyRecruitingStudies(Pageable pageable, Long memberId) {
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndIsOwned(memberId, true);

        List<Study> studies = studyRepository.findRecruitingStudiesByMemberStudy(memberStudies, pageable);

        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, studies.size(), memberId);
    }

    private static Map<String, Object> getSearchConditions(SearchRequestStudyDTO request) {
        log.info("request: {}", request.getIsOnline());
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

    private static SearchResponseDTO.StudyPreviewDTO getDTOs(List<Study> studies, Pageable pageable, long totalElements,
        Long memberId) {
        // memberId == null 이면, 다른 생성자 사용
        List<SearchResponseDTO.SearchStudyDTO> stream = studies.stream()
            .map((Study study) -> memberId == null ? new SearchStudyDTO(study) : new SearchStudyDTO(study, memberId))
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

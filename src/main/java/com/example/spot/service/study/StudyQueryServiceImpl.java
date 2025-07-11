package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Status;
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
import com.example.spot.repository.RegionRepository;
import com.example.spot.repository.RegionStudyRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.repository.StudyThemeRepository;
import com.example.spot.repository.ThemeRepository;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.search.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchRequestStudyWithThemeDTO;
import com.example.spot.web.dto.search.SearchResponseDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.HotKeywordDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.MyPageDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.SearchStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import com.example.spot.web.dto.search.StudyHistoryResponseDTO;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StudyQueryServiceImpl implements StudyQueryService {


    @Value("${study.hot-keyword}")
    private String HOT_KEYWORD;
    @Value("${study.last-updated}")
    private String LAST_UPDATED;

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
    private final RegionRepository regionRepository;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 인기 검색어를 조회하는 메서드입니다. 인기 검색어는 매일 13시, 18시에 총 2번 업데이트 됩니다.
     * 인기 검색어는 검색된 횟수 순으로 5개까지 조회 가능합니다.
     * @return 인기 검색어 목록 및 업데이트 시간을 반환합니다.
     */
    @Override
    public HotKeywordDTO getHotKeyword() {
        // popular_keywords에서 캐시된 검색어 목록 가져오기
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        Set<TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(HOT_KEYWORD, 0, 4);

        // 캐시된 검색어가 없을 경우
        if (typedTuples.isEmpty())
            throw new GeneralException(ErrorStatus._HOT_KEYWORD_NOT_FOUND);

        // 순서를 보장하는 LinkedHashSet을 사용합니다.
        Set<HotKeywordDTO.KeywordDTO> keywordDTOS = new LinkedHashSet<>();

        // DTO로 변환
        for (TypedTuple<String> tuple : typedTuples) {
            keywordDTOS.add(HotKeywordDTO.KeywordDTO.builder()
                .keyword(tuple.getValue())
                .point(tuple.getScore())
                .build());
        }

        // 마지막 업데이트 시간 가져오기
        String updatedAt = redisTemplate.opsForValue().get(LAST_UPDATED);

        return HotKeywordDTO.builder()
            .keyword(keywordDTOS)
            .updatedAt(updatedAt) // 업데이트 시간 설정
            .build();
    }

    /**
     * 스터디의 상세 정보를 조회하는 메서드입니다
     * @param studyId 스터디의 아이디를 입력 받습니다.
     * @return 스터디의 상세 정보를 반환합니다.
     * @throws StudyHandler 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     */
    @Transactional
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
        study.increaseHit();
        return StudyInfoResponseDTO.StudyInfoDTO.toDTO(study, owner);
    }

    /**
     * 마이페이지에 들어갈 스터디 갯수 관련 정보를 조회 하는 메서드입니다.
     * @param memberId  회원의 아이디를 입력 받습니다.
     * @return 지원한 스터디, 참여중인 스터디, 모집중인 스터디 갯수를 반환합니다.
     * @throws MemberHandler 회원이 존재하지 않을 경우 Exception을 발생시킵니다.
     */
    @Override
    public MyPageDTO getMyPageStudyCount(Long memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 내가 신청한 스터디 수
        long appliedStudies = memberStudyRepository.countByMemberIdAndStatusAndStudy_Status(memberId, ApplicationStatus.APPLIED, Status.ON);
        // 내가 참여중인 스터디 수
        long ongoingStudies = memberStudyRepository.countByMemberIdAndStatusAndStudy_Status(memberId, ApplicationStatus.APPROVED, Status.ON);
        // 내가 모집중인 스터디 수
        long myRecruitingStudies = memberStudyRepository.countByMemberIdAndIsOwnedAndStudy_Status(memberId, true, Status.ON);

        return MyPageDTO.builder()
            .name(member.getName())
            .appliedStudies(appliedStudies)
            .ongoingStudies(ongoingStudies)
            .myRecruitingStudies(myRecruitingStudies)
            .build();
    }

    /**
     * 검색 조건 없이 전체 스터디를 조회하는 메서드입니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     */
    @Override
    public StudyPreviewDTO findStudies(Pageable pageable, StudySortBy sortBy) {
        // 스터디 전체 조회
        List<Study> studies = studyRepository.findAllStudy(sortBy, pageable);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.count();
        return getDTOs(studies, pageable, totalElements, SecurityUtils.getCurrentUserId());
    }

    /**
     * 검색 조건을 통해 전체 스터디를 조회하는 메서드입니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     * @param request 검색 조건을 입력 받습니다.
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     */
    @Override
    public StudyPreviewDTO findStudiesByConditions(Pageable pageable, SearchRequestStudyDTO request,
        StudySortBy sortBy) {
        // 검색 조건 맵 생성
        Map<String, Object> conditions = getSearchConditions(request);

        // 검색 조건에 맞는 스터디 조회
        List<Study> studies = studyRepository.findAllStudyByConditions(conditions, sortBy, pageable);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.countStudyByConditions(conditions, sortBy);
        return getDTOs(studies, pageable, totalElements, SecurityUtils.getCurrentUserId());
    }

    /**
     * 추천 스터디를 조회하는 메서드입니다.
     *
     * @param memberId 회원의 아이디를 입력 받습니다.
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     * @throws MemberHandler 회원의 관심사가 존재하지 않는 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 회원의 관심사에 해당하는 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     */
    @Override
    public StudyPreviewDTO findRecommendStudies(Long memberId) {

        // 회원이 참가하고 있는 스터디 ID 가져오기
        List<Long> memberOngoingStudyIds = getOngoingStudyIds(memberId);

        // 회원 관심사 조회
        List<MemberTheme> memberThemes = memberThemeRepository.findAllByMemberId(memberId);
        List<PreferredRegion> preferredRegions = preferredRegionRepository.findAllByMemberId(memberId);

        // 회원 관심사가 없을 경우
        if (memberThemes.isEmpty())
            throw new MemberHandler(ErrorStatus._STUDY_THEME_IS_INVALID);

        if (preferredRegions.isEmpty())
            throw new MemberHandler(ErrorStatus._STUDY_REGION_IS_INVALID);

        // MemberId로 회원 관심사 및 관심 지역 전체 조회
        List<Theme> themes = memberThemes.stream()
            .map(MemberTheme::getTheme)
            .toList();

        List<Region> regions = preferredRegions.stream()
                .map(PreferredRegion::getRegion)
                .toList();

        // 회원 관심사로 스터디 테마 조회
        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(theme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();

        // 회원 관심 지역으로 스터디 지역 조회
        List<RegionStudy> regionStudies = regions.stream()
                .flatMap(region -> regionStudyRepository.findAllByRegion(region).stream())
                .toList();

        // 해당 관심사에 해당하는 스터디가 존재하지 않을 경우
        if (studyThemes.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_THEME_NOT_EXIST);

        // 해당 관심 지역에 해당하는 스터디가 존재하지 않을 경우
        if (regionStudies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_REGION_NOT_EXIST);

        // 회원 관심사로 추천 스터디 조회
        List<Study> preferThemeStudies = studyRepository.findByStudyThemeAndNotInIds(studyThemes, memberOngoingStudyIds);

        // 회원 관심 지역으로 추천 스터디 조회
        List<Study> preferRegionStudies = studyRepository.findByRegionStudyAndNotInIds(regionStudies, memberOngoingStudyIds);

        // 추천 스터디가 없을 경우
        if (preferRegionStudies.isEmpty() || preferThemeStudies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 두 리스트를 합쳐서 중복 제거
        Set<Study> combinedStudies = new HashSet<>(preferThemeStudies);
        combinedStudies.addAll(preferRegionStudies);

        // 리스트 변환
        List<Study> studyList = new ArrayList<>(combinedStudies);

        // 랜덤으로 최대 3개 선택
        Collections.shuffle(studyList);
        List<Study> selectedStudies = studyList.stream()
                .limit(3)
                .collect(Collectors.toList());

        return getDTOs(selectedStudies, Pageable.unpaged(), selectedStudies.size(), memberId);
    }


    /**
     * 관심 Best 스터디를 조회하는 메서드입니다.
     *
     * @param memberId 회원의 아이디를 입력 받습니다.
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     * @throws StudyHandler 조회된 추천 스터디가 없을 경우 Exception을 발생시킵니다.
     */
    @Override
    public StudyPreviewDTO findInterestedStudies(Long memberId) {
        // 회원의 관심 Best 스터디 ID 가져오기
        List<Study> studies = studyRepository.findAllStudyByConditions(new HashMap<>(),
            StudySortBy.LIKED,
            PageRequest.of(0, 3));

        // 추천 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, Pageable.unpaged(), studies.size(), memberId);
    }

    /**
     * 회원의 관심 분야에 해당 되는 모든 스터디를 조회 합니다. 회원이 현재 진행중인 스터디는 제외합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     * @param request 검색 조건을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws MemberHandler 회원의 관심사가 존재하지 않는 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 회원의 관심사에 해당하는 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchRequestStudyDTO request, StudySortBy sortBy) {
        // 회원이 참가하고 있는 스터디 ID 가져오기
        List<Long> memberOngoingStudyIds = getOngoingStudyIds(memberId);

        // 회원 관심사 조회
        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId).stream()
            .map(MemberTheme::getTheme)
            .toList();

        // 회원의 관심사가 없을 경우
        if (themes.isEmpty())
            throw new MemberHandler(ErrorStatus._STUDY_THEME_IS_INVALID);

        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(theme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();

        // 해당 관심사에 해당하는 스터디가 존재하지 않을 경우
        if (studyThemes.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_THEME_NOT_EXIST);

        // 검색 조건 맵 생성
        Map<String, Object> conditions = getSearchConditions(request);

        // 검색 조건에 맞는 스터디 갯수 조회
        long totalElements = studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(
            conditions, studyThemes, sortBy, memberOngoingStudyIds);

        // 검색 조건에 맞는 스터디 조회
        List<Study> studies = studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(
            conditions, sortBy,
            pageable, studyThemes, memberOngoingStudyIds);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
    }


    /**
     * 회원의 특정 관심 분야에 해당 되는 모든 스터디를 조회 합니다. 회원이 현재 진행중인 스터디는 제외합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     * @param request 검색 조건을 입력 받습니다.
     * @param themeType 관심 분야를 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws MemberHandler 회원의 관심사가 존재하지 않는 경우 Exception을 발생시킵니다.
     * @throws MemberHandler 회원이 조회하려는 관심사가 서비스에 등록된 관심사와 일치하지 않는 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 회원의 관심사에 해당하는 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findInterestStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchRequestStudyDTO request, ThemeType themeType, StudySortBy sortBy) {

        // 회원이 참가하고 있는 스터디 ID 가져오기
        List<Long> memberOngoingStudyIds = getOngoingStudyIds(memberId);

        // 회원 관심사 조회
        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId)
            .stream()
            .map(MemberTheme::getTheme)
            .collect(Collectors.toList());

        // 회원의 관심사가 없을 경우
        if (themes.isEmpty())
            throw new MemberHandler(ErrorStatus._STUDY_THEME_IS_INVALID);

        // 회원이 입력한 관심사가 등록된 관심사와 다른 경우
        if (themes.stream().noneMatch(theme -> theme.getStudyTheme().equals(themeType)))
            throw new MemberHandler(ErrorStatus._BAD_REQUEST);


        // 회원 관심사로 스터디 테마 조회
        Theme theme = findThemeByType(themes, themeType);

        // 스터디 테마 조회
        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(studytheme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();

        // 해당 관심사에 해당하는 스터디가 존재하지 않을 경우
        if (studyThemes.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_THEME_NOT_EXIST);

        // 검색 조건 맵 생성
        Map<String, Object> conditions = getSearchConditions(request);

        // 검색 조건에 맞는 스터디 갯수 조회
        long totalElements = studyRepository.countStudyByConditionsAndThemeTypesAndNotInIds(
            conditions, studyThemes, sortBy, memberOngoingStudyIds);

        // 검색 조건에 맞는 스터디 조회
        List<Study> studies = studyRepository.findStudyByConditionsAndThemeTypesAndNotInIds(
            conditions, sortBy, pageable, studyThemes, memberOngoingStudyIds);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
    }


    /**
     * 회원의 관심 지역에 해당 되는 모든 스터디를 조회 합니다. 회원이 현재 진행중인 스터디는 제외합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     * @param request 검색 조건을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws MemberHandler 회원의 관심 지역이 존재하지 않는 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 회원의 관심 지역에 해당하는 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findInterestRegionStudiesByConditionsAll(
            Pageable pageable, Long memberId, SearchRequestStudyWithThemeDTO request, StudySortBy sortBy) {

        // 회원이 참가하고 있는 스터디 ID 가져오기
        List<Long> memberOngoingStudyIds = getOngoingStudyIds(memberId);


        // 회원 관심 지역 조회
        List<Region> regions = preferredRegionRepository.findAllByMemberId(memberId).stream()
            .map(PreferredRegion::getRegion)
            .toList();

        // 회원의 관심 지역이 없을 경우이
        if (regions.isEmpty())
            throw new MemberHandler(ErrorStatus._STUDY_REGION_IS_INVALID);

        // 회원 관심 지역으로 스터디 지역 조회
        List<RegionStudy> regionStudies = regions.stream()
            .flatMap(region -> regionStudyRepository.findAllByRegion(region).stream())
            .toList();

        // 해당 관심 지역에 해당하는 스터디가 존재하지 않을 경우
        if (regionStudies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_REGION_NOT_EXIST);

        // 검색 조건 맵 생성
        Map<String, Object> conditions = getSearchConditionsWithTheme(request);


        // 검색 조건에 맞는 스터디 갯수 조회
        long totalElements = studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(
            conditions, regionStudies, sortBy, memberOngoingStudyIds);

        // 검색 조건에 맞는 스터디 조회
        List<Study> studies = studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(
            conditions, sortBy,
            pageable, regionStudies, memberOngoingStudyIds);

        //
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
    }

    /**
     * 회원의 특정 관심 지역에 해당 되는 모든 스터디를 조회 합니다. 회원이 현재 진행중인 스터디는 제외합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     * @param request 검색 조건을 입력 받습니다.
     * @param regionCode 관심 지역 코드를 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws MemberHandler 회원의 관심 지역이 존재하지 않는 경우 Exception을 발생시킵니다.
     * @throws MemberHandler 회원이 조회하려는 관심 지역이 서비스에 등록된 관심 지역과 일치하지 않는 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 회원의 관심 지역에 해당하는 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findInterestRegionStudiesByConditionsSpecific(
            Pageable pageable, Long memberId, SearchRequestStudyWithThemeDTO request, String regionCode, StudySortBy sortBy) {

        // 회원이 참가하고 있는 스터디 ID 가져오기
        List<Long> memberOngoingStudyIds = getOngoingStudyIds(memberId);

        // 회원 관심 지역 조회
        List<Region> regions = preferredRegionRepository.findAllByMemberId(memberId)
            .stream()
            .map(PreferredRegion::getRegion)
            .toList();

        // 회원의 관심 지역이 없을 경우
        if (regions.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_REGION_IS_INVALID);

        // 회원이 입력한 관심 지역이 등록된 지역과 다른 경우
        if (regions.stream().noneMatch(region -> region.getCode().equals(regionCode)))
            throw new StudyHandler(ErrorStatus._STUDY_REGION_IS_NOT_MATCH);

        // 회원 관심 지역으로 스터디 지역 조회
        Region region = findRegionByCode(regions, regionCode);

        // 스터디 지역 조회
        List<RegionStudy> regionStudies = regions.stream()
            .flatMap(regionStudy -> regionStudyRepository.findAllByRegion(region).stream())
            .toList();

        // 해당 관심 지역에 해당하는 스터디가 존재하지 않을 경우
        if (regionStudies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_REGION_NOT_EXIST);

        // 검색 조건 맵 생성
        Map<String, Object> conditions = getSearchConditionsWithTheme(request);

        // 검색 조건에 맞는 스터디 갯수 조회
        long totalElements = studyRepository.countStudyByConditionsAndRegionStudiesAndNotInIds(
            conditions, regionStudies, sortBy, memberOngoingStudyIds);

        // 검색 조건에 맞는 스터디 조회
        List<Study> studies = studyRepository.findStudyByConditionsAndRegionStudiesAndNotInIds(
            conditions, sortBy, pageable, regionStudies, memberOngoingStudyIds);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        return getDTOs(studies, pageable, totalElements, memberId);
    }

    /**
     * 모집중인 스터디를 조회합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param request 검색 조건을 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findRecruitingStudiesByConditions(
            Pageable pageable, SearchRequestStudyWithThemeDTO request, StudySortBy sortBy) {

        // 검색 조건 맵 생성
        Map<String, Object> conditions = getSearchConditionsWithTheme(request);

        // 검색 조건(모집 중)에 맞는 스터디 조회
        List<Study> studies = studyRepository.findRecruitingStudyByConditions(conditions,
            sortBy, pageable);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.countRecruitingStudyByConditions(conditions, sortBy);
        return getDTOs(studies, pageable, totalElements, SecurityUtils.getCurrentUserId());
    }

    /**
     * 특정 회원이 좋아요 한 스터디를 조회합니다.
     *
     * @param memberId 회원의 아이디를 입력 받습니다.
     * @param pageable 페이지 정보를 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 좋아요 한 스터디가 없는 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findLikedStudies(Long memberId, Pageable pageable) {
        // 회원이 좋아요한 스터디 조회
        List<PreferredStudy> preferredStudyList = preferredStudyRepository.findByMemberIdAndStudyLikeStatusOrderByCreatedAtDesc(
            memberId, StudyLikeStatus.LIKE, pageable);

        // 좋아요한 스터디가 없을 경우
        if (preferredStudyList.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_LIKED_NOT_FOUND);

        // 좋아요한 스터디 목록
        List<Study> studies = preferredStudyList.stream()
            .map(PreferredStudy::getStudy)
            .toList();

        // 전체 스터디 수
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = preferredStudyRepository.countByMemberIdAndStudyLikeStatus(memberId, StudyLikeStatus.LIKE);
        return getDTOs(studies, pageable, totalElements, memberId);
    }

    /**
     * 입력 받은 키워드가 제목에 포함된 스터디를 조회 합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param keyword  검색 키워드를 입력 받습니다.
     * @param sortBy  정렬 기준을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findStudiesByKeyword(Pageable pageable,
        String keyword, StudySortBy sortBy) {
        // 키워드로 스터디 조회
        List<Study> studies = studyRepository.findAllByTitleContaining(keyword, sortBy, pageable);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.countAllByTitleContaining(keyword, sortBy);
        return getDTOs(studies, pageable, totalElements, SecurityUtils.getCurrentUserId());
    }

    /**
     * 입력 받은 테마에 해당하는 스터디를 조회 합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param theme    테마를 입력 받습니다.
     * @param sortBy   정렬 기준을 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 해당 테마에 해당하는 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findStudiesByTheme(Pageable pageable, ThemeType theme, StudySortBy sortBy) {
        // 테마로 스터디 조회
        Theme themeEntity = themeRepository.findByStudyTheme(theme)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));

        // 테마에 해당하는 스터디 테마 조회
        List<StudyTheme> studyThemes = studyThemeRepository.findAllByTheme(themeEntity);

        // 해당 테마에 해당하는 스터디가 존재하지 않을 경우
        if (studyThemes.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_THEME_NOT_EXIST);

        // 테마에 해당하는 스터디 조회
        List<Study> studies = studyRepository.findByStudyTheme(studyThemes, sortBy, pageable);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.countStudyByStudyTheme(studyThemes, sortBy);
        return getDTOs(studies, pageable, totalElements, SecurityUtils.getCurrentUserId());
    }

    /**
     * 입력 받은 회원이 참가하고 있는 스터디를 조회 합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 회원이 참가하고 있는 스터디가 없을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findOngoingStudiesByMemberId(Pageable pageable, Long memberId) {
        // 회원이 참가하고 있는 스터디 ID 가져오기
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(
            memberId, ApplicationStatus.APPROVED);

        // 회원이 참가하고 있는 스터디가 없을 경우
        if (memberStudies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_NOT_PARTICIPATED);

        // 회원이 참가하고 있는 스터디 조회
        List<Study> studies = studyRepository.findByMemberStudiesAndStatus(memberStudies, pageable, Status.ON);

        // 스터디가 끝났으면 제외
        studies = studies.stream()
                .filter(study -> study.getStatus().equals(Status.ON))
                .toList();

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.countByMemberStudiesAndStatus(memberStudies, Status.ON);
        return getDTOs(studies, pageable, totalElements, memberId);
    }

    /**
     * 특정 회원이 신청한 스터디를 조회합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 회원이 신청한 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findAppliedStudies(Pageable pageable, Long memberId) {
        // 회원이 신청한 스터디 조회
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(
            memberId, ApplicationStatus.APPLIED);

        // 회원이 신청한 스터디가 없을 경우
        if (memberStudies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_NOT_APPLIED);

        // 회원이 신청한 스터디 조회
        List<Study> studies = studyRepository.findByMemberStudiesAndStatus(memberStudies, pageable, Status.ON);

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.countByMemberStudiesAndStatus(memberStudies, Status.ON);
        return getDTOs(studies, pageable, totalElements, memberId);
    }

    /**
     * 특정 회원이 모집 중인 스터디를 조회합니다.
     *
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws StudyHandler 회원이 모집 중인 스터디가 존재하지 않을 경우 Exception을 발생시킵니다.
     * @throws StudyHandler 조회된 스터디가 없을 경우 Exception을 발생시킵니다.
     *
     */
    @Override
    public StudyPreviewDTO findMyRecruitingStudies(Pageable pageable, Long memberId) {
        // 회원이 모집중인 스터디 조회
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndIsOwned(memberId, true);

        // 회원이 모집중인 스터디가 없을 경우
        if (memberStudies.isEmpty())
            throw new StudyHandler(ErrorStatus._RECRUITING_STUDY_IS_NOT_EXIST);

        // 회원이 모집중인 스터디 조회
        List<Study> studies = studyRepository.findRecruitingStudiesByMemberStudy(memberStudies, pageable);

        studies = studies.stream()
                .filter(study -> study.getStatus().equals(Status.ON))
                .toList();

        // 조회된 스터디가 없을 경우
        if (studies.isEmpty())
            throw new StudyHandler(ErrorStatus._STUDY_IS_NOT_MATCH);

        // 전체 스터디 수
        long totalElements = studyRepository.countByMemberStudiesAndStatusAndIsOwned(memberStudies, Status.ON, true);

        return getDTOs(studies, pageable, totalElements, memberId);
    }

    @Override
    public StudyHistoryResponseDTO getFinishedStudies(Pageable pageable, Long currentUserId) {
        Member member = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Page<MemberStudy> finishedStudies = memberStudyRepository.findAllByMemberIdAndConditions(currentUserId, pageable);
        return StudyHistoryResponseDTO.of(finishedStudies);
    }

    /**
     * 특정 회원이 참가하고 있는 스터디를 조회합니다.
     *
     * @param memberId 회원의 아이디를 입력 받습니다.
     *
     * @return 입력한 조건에 맞는 스터디 목록과 조회된 스터디 갯수를 함께 반환합니다.
     *
     * @throws MemberHandler 회원이 존재하지 않을 경우 Exception을 발생시킵니다.
     *
     */
    private List<Long> getOngoingStudyIds(Long memberId) {
        // 회원 조회
        if (!memberRepository.existsById(memberId))
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);

        // 회원이 참가하고 있는 스터디 ID 가져오기
        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(memberId, ApplicationStatus.APPROVED);

        return memberStudies.stream()
            .filter(memberStudy -> memberStudy.getStatus().equals(ApplicationStatus.APPROVED))
                .filter(memberStudy -> memberStudy.getStudy().getStatus().equals(Status.ON))
            .map(memberStudy -> memberStudy.getStudy().getId())
            .toList();
    }


    /**
     * 검색 조건을 입력 받아 검색 조건 맵을 생성하는 메서드입니다.
     *
     * @param request 검색 조건을 입력 받습니다.
     *
     * @return 검색 조건 맵을 반환합니다.
     *
     */
    private Map<String, Object> getSearchConditions(SearchRequestStudyDTO request) {
        // 검색 조건 맵 생성
        return getBasicStudyFilteringConditions(request.getGender(), request.getMinAge(), request.getMaxAge(),
                request.getIsOnline(), request.getHasFee(), request.getMaxFee(), request.getMinFee(), request.getRegionCodes());
    }

    /**
     * 검색 조건을 입력 받아 검색 조건 맵을 생성하는 메서드입니다.
     *
     * @param request 검색 조건을 입력 받습니다.
     *
     * @return 검색 조건 맵을 반환합니다.
     *
     */
    private Map<String, Object> getSearchConditionsWithTheme(SearchRequestStudyWithThemeDTO request) {
        Map<String, Object> search = getBasicStudyFilteringConditions(request.getGender(), request.getMinAge(),
                request.getMaxAge(), request.getIsOnline(),
                request.getHasFee(), request.getMaxFee(), request.getMinFee(), request.getRegionCodes());

        if (request.getThemeTypes() != null && !request.getThemeTypes().isEmpty()) {
            search.put("themeTypes", request.getThemeTypes());
        }

        return search;
    }

    private Map<String, Object> getBasicStudyFilteringConditions(Gender gender, Integer minAge, Integer maxAge,
                                                                        Boolean isOnline, Boolean hasFee,
                                                                        Integer maxFee, Integer minFee, List<String> regions) {
        Map<String, Object> search = new HashMap<>();

        if (gender != null)
            search.put("gender", gender);
        if (minAge != null)
            search.put("minAge", minAge);
        if (maxAge != null)
            search.put("maxAge", maxAge);
        if (isOnline != null)
            search.put("isOnline", isOnline);
        if (hasFee != null)
            search.put("hasFee", hasFee);
        if (maxFee != null)
            search.put("maxFee", maxFee);
        if (minFee != null)
            search.put("minFee", minFee);

        // 지역 코드가 null 이거나 비어있으면 검색 조건에 추가하지 않음
        if (regions == null || regions.isEmpty())
            return search;

        List<Region> regionList = convertCodeToRegion(regions);

        if (regionList != null && !regionList.isEmpty())
            search.put("regions", regionList);
        return search;
    }

    private List<Region> convertCodeToRegion(List<String> regions) {
        List<Region> regionList = regions.stream()
            .map(regionCode -> regionRepository.findByCode(regionCode)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_REGION_NOT_EXIST)))
            .toList();
        return regionList;
    }


    /**
     * 스터디 목록을 DTO로 변환하는 메서드입니다.
     *
     * @param studies  스터디 목록을 입력 받습니다.
     * @param pageable 페이지 정보를 입력 받습니다.
     * @param totalElements 전체 스터디 수를 입력 받습니다.
     * @param memberId 회원의 아이디를 입력 받습니다.
     *
     * @return 스터디 목록을 DTO로 변환하여 반환합니다.
     */
    private static SearchResponseDTO.StudyPreviewDTO getDTOs(List<Study> studies, Pageable pageable, long totalElements,
        Long memberId) {
        // memberId == null 이면, 다른 생성자 사용
        List<SearchResponseDTO.SearchStudyDTO> stream = studies.stream()
            .map((Study study) -> memberId == null ? new SearchStudyDTO(study) : new SearchStudyDTO(study, memberId))
            .toList();
        Page<SearchResponseDTO.SearchStudyDTO> page = new PageImpl<>(stream, pageable, totalElements);
        return new StudyPreviewDTO(page, stream, totalElements);
    }

    /**
     * 테마 타입으로 저장된 테마를 조회합니다.
     *
     * @param themes 테마 목록을 입력 받습니다.
     * @param themeType 테마 타입을 입력 받습니다.
     *
     * @return 테마 타입에 해당하는 테마를 반환합니다.
     */
    private Theme findThemeByType(List<Theme> themes, ThemeType themeType) {
        return themes.stream()
            .filter(t -> t.getStudyTheme().equals(themeType))
            .findFirst()
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));
    }

    /**
     * 지역 코드로 저장된 지역을 조회합니다.
     *
     * @param regions 지역 목록을 입력 받습니다.
     * @param regionCode 지역 코드를 입력 받습니다.
     *
     * @return 지역 코드에 해당하는 지역을 반환합니다.
     */
    private Region findRegionByCode(List<Region> regions, String regionCode) {
        return regions.stream()
            .filter(r -> r.getCode().equals(regionCode))
            .findFirst()
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));
    }

}

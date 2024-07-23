package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberTheme;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.MemberThemeRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.repository.StudyThemeRepository;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
public class StudyQueryServiceImpl implements StudyQueryService {

    private final StudyRepository studyRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final MemberThemeRepository memberThemeRepository;
    private final MemberRepository memberRepository;
    private final StudyThemeRepository studyThemeRepository;

    @Override
    public Page<SearchResponseDTO.SearchStudyDTO> findRecommendStudies(Long memberId) {

        // MemberId로 회원 관심사 전체 조회
        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId).stream()
            .map(MemberTheme::getTheme)
            .toList();

        // 회원 관심사로 스터디 테마 조회
        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(theme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();
        List<Study> studies = studyRepository.findByStudyTheme(studyThemes);

        List<SearchResponseDTO.SearchStudyDTO> stream = getDtos(studies);

        return new PageImpl<>(stream);
    }

    @Override
    public Page<SearchResponseDTO.SearchStudyDTO> findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchStudyDTO request) {

        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId).stream()
            .map(MemberTheme::getTheme)
            .toList();

        List<StudyTheme> studyThemes = themes.stream()
            .flatMap(theme -> studyThemeRepository.findAllByTheme(theme).stream())
            .toList();

        Map<String, Object> conditions = getSearchConditions(request);

        long totalElements = studyRepository.countStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(conditions, studyThemes);


        List<Study> studies = studyRepository.findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(
            conditions, request.getSortBy(),
            pageable, studyThemes);

        List<SearchResponseDTO.SearchStudyDTO> stream = getDtos(studies);

        return new PageImpl<>(stream, pageable, totalElements);
    }

    private Theme findThemeByType(List<Theme> themes, ThemeType themeType) {
        return themes.stream()
            .filter(t -> t.getStudyTheme().equals(themeType))
            .findFirst()
            .orElseThrow(() -> new StudyHandler(ErrorStatus._BAD_REQUEST));
    }

    @Override
    public Page<SearchResponseDTO.SearchStudyDTO> findInterestStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchStudyDTO request, ThemeType themeType) {

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

        long totalElements = studyRepository.countStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(conditions, studyThemes);

        List<Study> studies = studyRepository.findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(
            conditions, request.getSortBy(), pageable, studyThemes);

        List<SearchResponseDTO.SearchStudyDTO> studyDTOs = getDtos(studies);

        return new PageImpl<>(studyDTOs, pageable, totalElements);
    }


    @Override
    public Page<SearchResponseDTO.SearchStudyDTO> findInterestRegionStudiesByConditionsAll(Pageable pageable,
        Long memberId, SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<SearchResponseDTO.SearchStudyDTO> findInterestRegionStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchStudyDTO request, String regionCode) {
        return null;
    }

    @Override
    public Page<SearchResponseDTO.SearchStudyDTO> findRecruitingStudiesByConditions(Pageable pageable,
        SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<SearchResponseDTO.SearchStudyDTO> findLikedStudiesByConditions(Pageable pageable, Long memberId) {
        return null;
    }

    private static Map<String, Object> getSearchConditions(SearchStudyDTO request) {
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

    private static List<SearchResponseDTO.SearchStudyDTO> getDtos(List<Study> studies) {
        List<SearchResponseDTO.SearchStudyDTO> stream = studies.stream()
            .map(SearchResponseDTO.SearchStudyDTO::new)
            .toList();
        return stream;
    }

}

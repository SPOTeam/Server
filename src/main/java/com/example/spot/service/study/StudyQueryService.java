package com.example.spot.service.study;

import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.SearchStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyQueryService {

    // 내 추천 스터디 조회
    StudyPreviewDTO findRecommendStudies(Long memberId);

    // 내 관심사 스터디 페이징 조회
    StudyPreviewDTO findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchRequestStudyDTO request, StudySortBy sortBy);

    // 내 특정 관심사 스터디 페이징 조회
    StudyPreviewDTO findInterestStudiesByConditionsSpecific(Pageable pageable, Long memberId,
        SearchRequestStudyDTO request, ThemeType theme, StudySortBy sortBy);

    // 내 관심 지역 스터디 페이징 조회
    StudyPreviewDTO findInterestRegionStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchRequestStudyDTO request, StudySortBy sortBy);

    // 내 특정 관심 지역 스터디 페이징 조회
    StudyPreviewDTO findInterestRegionStudiesByConditionsSpecific(Pageable pageable, Long memberId,
        SearchRequestStudyDTO request, String regionCode, StudySortBy sortBy);

    // 모집 중 스터디 조회
    StudyPreviewDTO findRecruitingStudiesByConditions(Pageable pageable,
        SearchRequestStudyDTO request, StudySortBy sortBy);

    // 찜한 스터디 조회
    StudyPreviewDTO findLikedStudies(Long memberId);

    // 스터디 키워드 검색
    StudyPreviewDTO findStudiesByKeyword(Pageable pageable, String keyword, StudySortBy sortBy);

    // 테마 별 스터디 검색
    StudyPreviewDTO findStudiesByTheme(Pageable pageable, ThemeType theme, StudySortBy sortBy);

}

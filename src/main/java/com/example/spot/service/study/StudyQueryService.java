package com.example.spot.service.study;

import com.example.spot.domain.enums.ThemeType;
import com.example.spot.web.dto.search.SearchRequestDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.SearchStudyDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyQueryService {

    // 내 추천 스터디 조회
    Page<SearchRequestDTO.SearchStudyDTO> findRecommendStudies(Pageable pageable, Long memberId);

    // 내 관심사 스터디 페이징 조회
    Page<SearchStudyDTO> findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchRequestDTO.SearchStudyDTO request);

    // 내 특정 관심사 스터디 페이징 조회
    Page<SearchRequestDTO.SearchStudyDTO> findInterestStudiesByConditionsSpecific(Pageable pageable, Long memberId,
        SearchRequestDTO.SearchStudyDTO request, ThemeType theme);

    // 내 관심 지역 스터디 페이징 조회
    Page<SearchRequestDTO.SearchStudyDTO> findInterestRegionStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchRequestDTO.SearchStudyDTO request);

    // 내 특정 관심 지역 스터디 페이징 조회
    Page<SearchRequestDTO.SearchStudyDTO> findInterestRegionStudiesByConditionsSpecific(Pageable pageable, Long memberId,
        SearchRequestDTO.SearchStudyDTO request, String regionCode);

    // 모집 중 스터디 조회
    Page<SearchRequestDTO.SearchStudyDTO> findRecruitingStudiesByConditions(Pageable pageable,
        SearchRequestDTO.SearchStudyDTO request);

    // 찜한 스터디 조회
    Page<SearchRequestDTO.SearchStudyDTO> findLikedStudiesByConditions(Pageable pageable, Long memberId);

}

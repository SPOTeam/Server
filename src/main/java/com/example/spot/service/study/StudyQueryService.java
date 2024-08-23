package com.example.spot.service.study;

import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.MyPageDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO;
import com.example.spot.web.dto.study.response.StudyPostResponseDTO;
import com.example.spot.web.dto.study.response.StudyScheduleResponseDTO;
import org.springframework.data.domain.Pageable;

public interface StudyQueryService {

    // 스터디 정보 조회
    StudyInfoResponseDTO.StudyInfoDTO getStudyInfo(Long studyId);

    // 마이페이지 용 스터디 갯수 조회
    MyPageDTO getMyPageStudyCount(Long memberId);

    StudyPreviewDTO findStudies(Pageable pageable, StudySortBy sortBy);

    StudyPreviewDTO findStudiesByConditions(Pageable pageable, SearchRequestStudyDTO request, StudySortBy sortBy);

    // 내 추천 스터디 조회
    StudyPreviewDTO findRecommendStudies(Long memberId);

    StudyPreviewDTO findInterestedStudies(Long memberId);

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
    StudyPreviewDTO findLikedStudies(Long memberId, Pageable pageable);

    // 스터디 키워드 검색
    StudyPreviewDTO findStudiesByKeyword(Pageable pageable, String keyword, StudySortBy sortBy);

    // 테마 별 스터디 검색
    StudyPreviewDTO findStudiesByTheme(Pageable pageable, ThemeType theme, StudySortBy sortBy);

    // 내가 참여하고 있는 스터디 조회
    StudyPreviewDTO findOngoingStudiesByMemberId(Pageable pageable, Long memberId);

    // 내가 신청한 스터디 조회
    StudyPreviewDTO findAppliedStudies(Pageable pageable, Long memberId);

    // 내가 모집중인 스터디 조회
    StudyPreviewDTO findMyRecruitingStudies(Pageable pageable, Long memberId);

}

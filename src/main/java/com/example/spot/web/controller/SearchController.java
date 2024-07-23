package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.service.study.StudyQueryService;
import com.example.spot.web.dto.search.SearchRequestDTO;
import com.example.spot.web.dto.search.SearchResponseDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.SearchStudyDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search", description = "Search API")
@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
public class SearchController {

    private final StudyQueryService studyQueryService;

    @GetMapping("/search/studies/recommend/main/users/{userId}")
    @Operation(summary = "[메인 화면 - 개발중] 회원 별 추천 스터디 3개 조회",
        description = """
            ## [메인 화면] 접속한 회원의 추천 스터디 3개를 조회 합니다.
            조회된 스터디 3개의 정보가 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    public void recommendStudiesForMain(@PathVariable long userId) {}


    /* ----------------------------- 내 관심 분야 별 스터디 조회  ------------------------------------- */


    @GetMapping("/search/studies/interest-themes/all/users/{userId}")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] 내 '전체' 관심사 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 입력한 조건에 맞는 회원의 전체 관심 분야의 스터디를 조회 합니다. 
            메인 화면에서 사용 하실 경우, 페이지 번호는 0, 페이지 크기는 3으로 설정하여 사용하시면 됩니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "searchStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    - sortBy: 정렬 기준 (ALL, RECRUITING, HIT, LIKED)
    """, required = false)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public ApiResponse<Page<SearchStudyDTO>> interestStudiesByConditionsAll(
        @PathVariable long userId,
        @ModelAttribute SearchRequestDTO.SearchStudyDTO searchStudyDTO,
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        Page<SearchStudyDTO> studies = studyQueryService.findInterestStudiesByConditionsAll(PageRequest.of(page, size), userId, searchStudyDTO);
        // 메소드 구현
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_UPDATED, studies);
    }

    @GetMapping("/search/studies/interest-themes/specific/users/{userId}/")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] 내 '특정' 관심사 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 입력한 조건에 맞는 회원의 특정 관심 분야의 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "searchStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    - sortBy: 정렬 기준 (ALL, RECRUITING, HIT, LIKED)
    """, required = false)
    @Parameter(name = "theme", description = "조회할 관심 분야를 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public void interestStudiesByConditionsSpecific(
        @PathVariable long userId,
        @RequestParam ThemeType theme,
        @ModelAttribute SearchRequestDTO.SearchStudyDTO searchStudyDTO,
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        // 메소드 구현
    }


    /* ----------------------------- 내 관심 지역 별 스터디 조회  ------------------------------------- */


    @GetMapping("/search/studies/preferred-region/all/users/{userId}")
    @Operation(
        summary = "[내 관심 지역 스터디 조회 - 개발중] 내 '전체' 관심 지역 스터디 조회",
        description = """
            ## [내 관심 지역 스터디 조회] 입력한 조건에 맞는 회원의 전체 관심 지역의 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "searchStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    - sortBy: 정렬 기준 (ALL, RECRUITING, HIT, LIKED)
    """, required = false)
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public void interestRegionStudiesByConditionsAll(
        @PathVariable long userId,
        @ModelAttribute SearchRequestDTO.SearchStudyDTO searchStudyDTO,
        @RequestParam Integer page,
        @RequestParam Integer size

    ) {
        // 메소드 구현
    }

    @GetMapping("/search/studies/preferred-region/specific/users/{userId}")
    @Operation(
        summary = "[내 관심 지역 스터디 조회 - 개발중] 내 '특정' 관심 지역 스터디 조회",
        description = """
            ## [내 관심 지역 스터디 조회] 입력한 조건에 맞는 회원의 특정 관심 지역의 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "searchStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    - sortBy: 정렬 기준 (ALL, RECRUITING, HIT, LIKED)
    """, required = false)
    @Parameter(name = "regionCode", description = "조회할 지역 코드를 입력 받습니다. 지역 코드는 5자리의 문자열 입니다. ex) 11000", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public void interestRegionStudiesByConditionsSpecific(
        @PathVariable long userId,
        @RequestParam String regionCode,
        @ModelAttribute SearchRequestDTO.SearchStudyDTO searchStudyDTO,
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        // 메소드 구현
    }

    /* ----------------------------- 모집 중 스터디 조회  ------------------------------------- */


    @GetMapping("/search/studies/recruiting")
    @Operation(
        summary = "[모집 중 스터디 조회 - 개발중] 모집 중인 스터디 조회",
        description = """
            ## [모집 중 스터디 조회] 입력한 조건에 맞는 모집 중인  스터디 전체를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다."""
    )
    @Parameter(name = "searchStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    - sortBy: 정렬 기준 (ALL, RECRUITING, HIT, LIKED)
    """, required = false)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public ApiResponse<String> recruitingStudiesByConditions(@ModelAttribute SearchRequestDTO.SearchStudyDTO searchStudyDTO,
        @RequestParam Integer page, @RequestParam Integer size) {
        // 메소드 구현
        return ApiResponse.onFailure(ErrorStatus._INTERNAL_SERVER_ERROR.getCode(), ErrorStatus._INTERNAL_SERVER_ERROR.getMessage(), null);
    }

    /* ----------------------------- 찜한 스터디 검색  ------------------------------------- */

    @GetMapping("/search/studies/liked/users/{userId}")
    @Operation(
        summary = "[찜한 스터디 조회 - 개발중] 찜한 스터디 조회",
        description = """
            ## [찜한 스터디 조회] 찜한 스터디 전체를 조회 합니다.
            찜한 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public ApiResponse<String> likedStudies(@PathVariable long userId, @RequestParam Integer page, @RequestParam Integer size) {
        // 메소드 구현
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, "찜한 스터디 목록 조회 성공");
    }
}

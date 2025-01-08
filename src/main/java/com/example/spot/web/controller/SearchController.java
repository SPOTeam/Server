package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.study.StudyCommandService;
import com.example.spot.service.study.StudyQueryService;
import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchRequestStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.HotKeywordDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.MyPageDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.StudyPreviewDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SearchController {

    private final StudyQueryService studyQueryService;
    private final StudyCommandService studyCommandService;


    /* ----------------------------- 메인 화면 ------------------------------------- */

    @Tag(name = "메인 화면", description = "메인 화면 API")
    @GetMapping("/search/studies/main/recommend")
    @Operation(summary = "[메인 화면] 회원 별 추천 스터디 3개 조회",
        description = """
            ## [메인 화면] 접속한 회원의 추천 스터디 3개를 조회 합니다.
            조회된 스터디 3개의 정보가 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    public ApiResponse<StudyPreviewDTO> recommendStudiesForMain() {
        StudyPreviewDTO recommendStudies = studyQueryService.findRecommendStudies(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, recommendStudies);
    }
    @Tag(name = "메인 화면", description = "메인 화면 API")
    @GetMapping("/search/studies/main/interested")
    @Operation(summary = "[메인 화면] 회원 별 관심 Best 스터디 3개 조회",
        description = """
            ## [메인 화면] 관심 Best 스터디 3개를 조회 합니다.
            조회된 스터디 3개의 정보가 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    public ApiResponse<StudyPreviewDTO> interestedStudiesForMain() {
        StudyPreviewDTO recommendStudies = studyQueryService.findInterestedStudies(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, recommendStudies);
    }

    @Tag(name = "마이 페이지", description = "마이 페이지 API")
    @GetMapping("/search/studies/my-page")
    @Operation(summary = "[마이 페이지] 마이 페이지 내 스터디 정보 조회",
        description = """
            ## [마이 페이지] 마이 페이지에 들어갈 나와 관련된 스터디 갯수 정보를 조회합니다.
            스터디 갯수 정보와 내 이름이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    public ApiResponse<MyPageDTO> myPage() {
        MyPageDTO myPageStudyCount = studyQueryService.getMyPageStudyCount(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND,  myPageStudyCount);
    }

    // 전체 스터디 조회
    @Tag(name = "전체 스터디 조회", description = "전체 스터디 조회 API")
    @GetMapping("/search/studies/all")
    @Operation(
        summary = "[전체 스터디 조회] 전체 스터디 조회",
        description = """
            ## [전체 스터디 조회] 입력한 조건에 맞는 전체 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다."""
    )
    public ApiResponse<StudyPreviewDTO> allStudiesByConditions(
        @ModelAttribute @Valid SearchRequestStudyDTO searchRequestStudyDTO,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy) {
        // 메소드 구현
        StudyPreviewDTO studies = studyQueryService.findStudiesByConditions(PageRequest.of(page, size),
            searchRequestStudyDTO, sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }

    @Tag(name = "전체 스터디 조회", description = "전체 스터디 조회 API")
    @GetMapping("/search/studies/all/no-conditions")
    @Operation(
        summary = "[전체 스터디 조회] 전체 스터디 조회 (조건 X)",
        description = """
            ## [전체 스터디 조회] 전체 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다."""
    )
    public ApiResponse<StudyPreviewDTO> allStudiesByConditions(
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy) {
        // 메소드 구현
        StudyPreviewDTO studies = studyQueryService.findStudies(PageRequest.of(page, size), sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }


    /* ----------------------------- 내 관심 분야 별 스터디 조회  ------------------------------------- */


    @Tag(name = "내 관심사 스터디 조회", description = "내 관심사 스터디 조회 API")
    @GetMapping("/search/studies/interest-themes/all")
    @Operation(
        summary = "[내 관심사 스터디 조회] 내 '전체' 관심사 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 입력한 조건에 맞는 회원의 전체 관심 분야의 스터디를 조회 합니다. 
            메인 화면에서 사용 하실 경우, 페이지 번호는 0, 페이지 크기는 3으로 설정하여 사용하시면 됩니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "searchRequestStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    """, required = false)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "sortBy", description = "정렬 기준을 입력 받습니다.", required = true)
    public ApiResponse<StudyPreviewDTO> interestStudiesByConditionsAll(
        @ModelAttribute @Valid SearchRequestStudyDTO searchRequestStudyDTO,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy
    ) {
        StudyPreviewDTO studies = studyQueryService.findInterestStudiesByConditionsAll(PageRequest.of(page, size),
                SecurityUtils.getCurrentUserId(), searchRequestStudyDTO, sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }
    @Tag(name = "내 관심사 스터디 조회", description = "내 관심사 스터디 조회 API")
    @GetMapping("/search/studies/interest-themes/specific")
    @Operation(
        summary = "[내 관심사 스터디 조회] 내 '특정' 관심사 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 입력한 조건에 맞는 회원의 특정 관심 분야의 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "searchRequestStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    """, required = false)
    @Parameter(name = "theme", description = "조회할 관심 분야를 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "sortBy", description = "정렬 기준을 입력 받습니다.", required = true)
    public ApiResponse<StudyPreviewDTO> interestStudiesByConditionsSpecific(
        @RequestParam ThemeType theme,
        @ModelAttribute @Valid SearchRequestStudyDTO searchRequestStudyDTO,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy
    ) {
        StudyPreviewDTO studies = studyQueryService.findInterestStudiesByConditionsSpecific(PageRequest.of(page, size),
                SecurityUtils.getCurrentUserId(), searchRequestStudyDTO, theme, sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
        // 메소드 구현
    }


    /* ----------------------------- 내 관심 지역 별 스터디 조회  ------------------------------------- */


    @Tag(name = "내 관심 지역 스터디 조회", description = "내 관심 지역 스터디 조회 API")
    @GetMapping("/search/studies/preferred-region/all")
    @Operation(
        summary = "[내 관심 지역 스터디 조회] 내 '전체' 관심 지역 스터디 조회",
        description = """
            ## [내 관심 지역 스터디 조회] 입력한 조건에 맞는 회원의 전체 관심 지역의 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "searchRequestStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    """, required = false)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "sortBy", description = "정렬 기준을 입력 받습니다.", required = true)
    public ApiResponse<StudyPreviewDTO> interestRegionStudiesByConditionsAll(
        @ModelAttribute @Valid SearchRequestStudyDTO searchRequestStudyDTO,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy

    ) {
        StudyPreviewDTO studies = studyQueryService.findInterestRegionStudiesByConditionsAll(
            PageRequest.of(page, size), SecurityUtils.getCurrentUserId(), searchRequestStudyDTO, sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }
    @Tag(name = "내 관심 지역 스터디 조회", description = "내 관심 지역 스터디 조회 API")
    @GetMapping("/search/studies/preferred-region/specific")
    @Operation(
        summary = "[내 관심 지역 스터디 조회] 내 '특정' 관심 지역 스터디 조회",
        description = """
            ## [내 관심 지역 스터디 조회] 입력한 조건에 맞는 회원의 특정 관심 지역의 스터디를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "searchRequestStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    """, required = false)
    @Parameter(name = "regionCode", description = "조회할 지역 코드를 입력 받습니다. 지역 코드는 10자리의 문자열 입니다. ex) 1111051500", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "sortBy", description = "정렬 기준을 입력 받습니다.", required = true)
    public ApiResponse<StudyPreviewDTO> interestRegionStudiesByConditionsSpecific(
        @RequestParam String regionCode,
        @ModelAttribute @Valid SearchRequestStudyDTO searchRequestStudyDTO,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy
    ) {
        StudyPreviewDTO studies = studyQueryService.findInterestRegionStudiesByConditionsSpecific(
            PageRequest.of(page, size), SecurityUtils.getCurrentUserId(), searchRequestStudyDTO, regionCode, sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }

    /* ----------------------------- 모집 중 스터디 조회  ------------------------------------- */


    @Tag(name = "모집 중 스터디 조회")
    @GetMapping("/search/studies/recruiting")
    @Operation(
        summary = "[모집 중 스터디 조회] 모집 중인 스터디 조회",
        description = """
            ## [모집 중 스터디 조회] 입력한 조건에 맞는 모집 중인  스터디 전체를 조회 합니다.
            조건에 맞게 검색된 스터디 목록이 반환 됩니다."""
    )
    @Parameter(name = "searchRequestStudyDTO", description = """
    조회할 스터디의 검색 조건을 입력 받습니다.
    - gender: 성별 (MALE, FEMALE, UNKNOWN)
    - minAge: 18 이상의 정수 
    - maxAge: 60 이하의 정수 
    - isOnline: 스터디 온라인 진행 여부 (true, false)
    - hasFee: 스터디 활동비 유무 (true, false)
    - fee: 스터디 최대 활동비 
    """, required = false)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "sortBy", description = "정렬 기준을 입력 받습니다.", required = true)
    public ApiResponse<StudyPreviewDTO> recruitingStudiesByConditions(
        @ModelAttribute @Valid SearchRequestStudyDTO searchRequestStudyDTO,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy) {
        // 메소드 구현
        StudyPreviewDTO studies = studyQueryService.findRecruitingStudiesByConditions(PageRequest.of(page, size),
            searchRequestStudyDTO, sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }

    // 내가 모집 중인 스터디 조회

    /* ----------------------------- 찜한 스터디 검색  ------------------------------------- */

    @Tag(name = "내 스터디 조회")
    @GetMapping("/search/studies/liked")
    @Operation(
        summary = "[찜한 스터디 조회] 찜한 스터디 조회",
        description = """
            ## [찜한 스터디 조회] 찜한 스터디 전체를 조회 합니다.
            찜한 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    public ApiResponse<StudyPreviewDTO> likedStudies(
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size) {
        StudyPreviewDTO studies = studyQueryService.findLikedStudies(SecurityUtils.getCurrentUserId(), PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }
    /* ----------------------------- 스터디 검색  ------------------------------------- */

    @Tag(name = "스터디 검색")
    @GetMapping("/search/studies")
    @Operation(
        summary = "[스터디 검색] 키워드를 통한 스터디 검색",
        description = """
            ## [스터디 검색] 제목에 키워드가 포함 되어 있는 스터디 전체를 조회 합니다.
            찜한 스터디 목록이 반환 됩니다."""
    )
    @Parameter(name = "keyword", description = "검색할 키워드를 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "sortBy", description = "정렬 기준을 입력 받습니다.", required = true)
    public ApiResponse<StudyPreviewDTO> searchStudiesByKeyword(
        @RequestParam String keyword,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy) {
        // 메소드 구현
        StudyPreviewDTO studies = studyQueryService.findStudiesByKeyword(PageRequest.of(page, size), keyword, sortBy);

        // 검색 키워드 저장 -> 검색이 성공적으로 이루어졌을 때만 저장.
        // 만약 키워드 검색 결과가 존재하지 않으면 저장하지 않음.
        studyCommandService.addHotKeyword(keyword);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }

    @Tag(name = "스터디 검색")
    @GetMapping("/search/studies/hot-keywords")
    @Operation(summary = "[스터디 검색] 인기 검색어 조회",
        description = """
            ## [스터디 검색] 현재 스터디 검색에 가장 많이 검색된 검색어를 조회합니다.
            인기 검색어는 13시, 18시에 초기화 됩니다. 초기화 된 시간과 현재 인기 검색어 목록을 반환합니다.
            """)
    public ApiResponse<HotKeywordDTO> getHotKeywords() {
        return ApiResponse.onSuccess(SuccessStatus._HOT_KEYWORD_FOUND, studyQueryService.getHotKeyword());
    }


    /* ----------------------------- 테마 별 스터디 검색  ------------------------------------- */
    @Tag(name = "스터디 검색")
    @GetMapping("/search/studies/theme/")
    @Operation(
        summary = "[테마 별 스터디 검색] 테마 별 스터디 검색",
        description = """
            ## [테마 별 스터디 검색] 입력한 테마에 해당하는 스터디 전체를 조회 합니다.
            테마 별 스터디 목록이 반환 됩니다."""
    )
    @Parameter(name = "theme", description = "검색할 테마를 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "sortBy", description = "정렬 기준을 입력 받습니다.", required = true)
    public ApiResponse<StudyPreviewDTO> searchStudiesByTheme(
        @RequestParam ThemeType theme,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam StudySortBy sortBy) {
        // 메소드 구현
        StudyPreviewDTO studies = studyQueryService.findStudiesByTheme(PageRequest.of(page, size), theme, sortBy);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }


    /* ----------------------------- 진행중인 스터디 목록 조회  ------------------------------------- */
    @Tag(name = "내 스터디 조회")
    @Operation(summary = "[참여하고 있는 스터디 조회] 내가 참여하고 있는 스터디 목록 불러오기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 클릭, 로그인한 회원이 참여하고 있는 스터디 목록을 불러옵니다.
        로그인한 회원이 참여하는 스터디 중 status = ON인 스터디의 목록이 반환됩니다.
        """)
    @GetMapping("/search/studies/on-studies")
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public ApiResponse<StudyPreviewDTO> getAllOnStudies(
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size) {
        StudyPreviewDTO studies = studyQueryService.findOngoingStudiesByMemberId(
            PageRequest.of(page, size), SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }

    /* ----------------------------- 모집중인 스터디 목록 조회  ------------------------------------- */

    @Tag(name = "내 스터디 조회")
    @Operation(summary = "[내가 모집중인 스터디] 내가 모집중인 스터디 목록 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 클릭, 로그인한 회원이 모집중인 스터디 목록을 불러옵니다.
        로그인한 회원이 운영하는 스터디 중 study_state = RECRUITING인 스터디의 목록이 반환됩니다.
        """)
    @GetMapping("/search/studies/my-recruiting-studies")
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public ApiResponse<StudyPreviewDTO> getAllMyRecruitingStudies(
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size) {
        StudyPreviewDTO studies = studyQueryService.findMyRecruitingStudies(
            PageRequest.of(page, size), SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }


    /* ----------------------------- 신청한 스터디 목록 조회  ------------------------------------- */
    @Tag(name = "내 스터디 조회")
    @Operation(summary = "[신청한 스터디] 신청한 스터디 목록 불러오기", description = """ 
        ## [신청한 스터디] 마이페이지 > 신청한, 로그인한 회원이 신청한 스터디 목록을 불러옵니다.
        로그인한 회원이 신청한 스터디(ApplicationStatus = APPLIED)의 목록이 반환됩니다.
        """)
    @GetMapping("/search/studies/applied-studies")
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public ApiResponse<StudyPreviewDTO> getAppliedStudies(
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size) {
        StudyPreviewDTO studies = studyQueryService.findAppliedStudies(
            PageRequest.of(page, size), SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, studies);
    }



}

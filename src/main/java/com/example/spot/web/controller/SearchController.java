package com.example.spot.web.controller;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search", description = "Search API")
@RestController
@RequestMapping("/spot")
public class SearchController {

    @GetMapping("/search/interest/main/{userId}")
    @Operation(summary = "[개발중] [메인 화면] 회원 관심 분야 스터디 3개 조회",
        description = "접속한 회원의 관심 스터디 3개를 조회 합니다. Path variable로 접속한 유저의 ID를 입력 받습니다.",
        security = @SecurityRequirement(name = "accessToken"))
    public void interestStudiesForMain(@PathVariable long userId) {}

    @GetMapping("/search/recommend/main/{userId}")
    @Operation(summary = "[개발중] [메인 화면] 회원 별 추천 스터디 3개 조회",
        description = "접속한 회원의 추천 스터디 3개를 조회 합니다. Path variable로 접속한 유저의 ID를 입력 받습니다.",
        security = @SecurityRequirement(name = "accessToken"))
    public void recommendStudiesForMain(@PathVariable long userId) {}

    @GetMapping("/search/interest/{userId}")
    @Operation(summary = "[개발중] 회원 별 '전체' 관심 분야 스터디 조회",
        description = "접속한 회원의 모든 관심 분야의 스터디를 전체 조회 합니다. Path variable로 접속한 유저의 ID를 입력 받습니다.",
        security = @SecurityRequirement(name = "accessToken"))
    public void interestStudies(@PathVariable long userId) {}

    @GetMapping("/search/interest/{userId}")
    @Operation(summary = "[개발중] 회원 별 '특정' 관심 분야 스터디 조회",
        description = "접속한 회원의 특정 관심 분야의 스터디를 조회 합니다. Path variable로 접속한 유저의 ID를 입력 받습니다. "
            + "Query parameter로 관심 분야를 입력 받습니다. "
            + "themeType은 어학, 자격증, 취업, 시사뉴스, 자율학습, 토론, 프로젝트, 공모전, 전공및진로학습, 기타 중 선택합니다."
            + "조건에 맞게 검색된 스터디 목록들이 반환 됩니다.",
        security = @SecurityRequirement(name = "accessToken"))
    public void interestStudiesByTopic(@PathVariable long userId, @RequestParam ThemeType themeType) {}

    @GetMapping("/search/interest/{userId}")
    @Operation(summary = "[개발중] 특정 조건 + 회원 별 관심 분야 스터디 조회",
        description = "입력한 조건에 맞는 접속한 회원의 특정 관심 분야의 스터디를 조회 합니다. "
            + "Path variable로 접속한 유저의 ID를 입력 받습니다. "
            + "Query parameter로 성별, 최소 나이, 최대 나이, 온라인 여부, 유료 여부를 입력 받습니다. "
            + "성별은 MALE, FEMALE, UNKNOWN으로 입력 받습니다. "
            + "minAge 18 이상,, maxAge는 60 이하의 정수를 입력 받습니다."
            + "isOnline과 hasFee은 true, false로 입력 받습니다."
            + "조건에 맞게 검색된 스터디 목록들이 반환 됩니다.",
        security = @SecurityRequirement(name = "accessToken"))
    public void interestStudiesByConditions(@PathVariable long userId,
                                            @RequestParam Gender gender,
                                            @RequestParam int minAge,
                                            @RequestParam int maxAge,
                                            @RequestParam boolean isOnline,
                                            @RequestParam boolean hasFee) {}






    @PostMapping("/search/recent-studies")
    @Operation(summary = "최근 검색 내역 저장하기", description = "회원의 최근 검색 내역을 임시 저장합니다.")
    public void saveRecentSearches() {}

    @GetMapping("/search/recent-studies")
    @Operation(summary = "최근 검색 내역 불러오기", description = "회원의 최근 검색 내역을 불러옵니다.")
    public void getRecentSearches() {}

    @GetMapping("/search/studies")
    @Operation(summary = "검색어를 포함한 스터디 불러오기", description = "검색어를 포함한 스터디 목록을 불러옵니다.")
    public void getStudySearches(@RequestParam("search") String search,
                                 @RequestParam("status") StudyState studyState) {}

    @GetMapping("/search/recommend-studies")
    @Operation(summary = "추천 스터디 불러오기", description = "회원에게 적합한 스터디 목록을 불러옵니다.")
    public void getRecommendStudies() {}

}

package com.example.spot.web.controller;

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

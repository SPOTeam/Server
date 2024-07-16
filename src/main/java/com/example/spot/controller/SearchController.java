package com.example.spot.controller;

import com.example.spot.domain.enums.StudyState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search", description = "Search API")
@RestController
@RequestMapping("/api")
public class SearchController {

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

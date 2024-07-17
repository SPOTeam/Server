package com.example.spot.web.controller;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search", description = "Search API")
@RestController
@RequestMapping("/spot")
public class SearchController {

    @GetMapping("/search/users/{userId}/interest-studies/main")
    @Operation(summary = "[메인 화면 - 개발중] 회원 별 관심 분야 스터디 3개 조회",
        description = """
            ## [메인 화면] 접속한 회원의 관심 스터디 3개를 조회 합니다.
            조회된 스터디 3개의 정보가 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "userId", description = "접속한 유저의 ID를 입력 받습니다.", required = true)
    public void interestStudiesForMain(@PathVariable long userId) {}

    @GetMapping("/search/users/{userId}/recommend-studies/main")
    @Operation(summary = "[메인 화면 - 개발중] 회원 별 추천 스터디 3개 조회",
        description = """
            ## [메인 화면] 접속한 회원의 추천 스터디 3개를 조회 합니다.
            조회된 스터디 3개의 정보가 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "userId", description = "접속한 유저의 ID를 입력 받습니다.", required = true)
    public void recommendStudiesForMain(@PathVariable long userId) {}

    @GetMapping("/search/users/{userId}/interest-studies/all")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] '전체' 관심 분야 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 접속한 회원의 모든 관심 분야의 스터디를 전체 조회 합니다.
            
            조회된 전체 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "접속한 유저의 ID를 입력 받습니다.", required = true)
    public void interestStudies(@PathVariable long userId) {
        // 메소드 구현
    }

    @GetMapping("/search/users/{userId}/interest-studies/specific")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] '특정' 관심 분야 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 접속한 회원의 특정 관심 분야의 스터디를 조회 합니다.
            
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "접속한 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "themeType", description = "관심분야를 입력 받습니다.", required = false)
    public void interestStudiesByTopic(
        @PathVariable long userId,
        @RequestParam ThemeType themeType
    ) {
        // 메소드 구현
    }

    @GetMapping("/search/users/{userId}/interest-studies/conditions/all")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] 특정 조건 + 내 '전체' 관심사 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 입력한 조건에 맞는 회원의 전체 관심 분야의 스터디를 조회 합니다.
            
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "접속한 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "gender", description = "성별을 입력 받습니다.", required = false)
    @Parameter(name = "minAge", description = "minAge는 18 이상의 정수 입니다.", required = false)
    @Parameter(name = "maxAge", description = "maxAge는 60 이하의 정수를 입력 받습니다.", required = false)
    @Parameter(name = "isOnline", description = "스터디 온라인 진행 여부 입니다. true, false의 bool 값으로 입력 받습니다", required = false)
    @Parameter(name = "hasFee", description = "스터디 활동비 유무 입니다. true, false의 bool 값으로 입력 받습니다", required = false)
    public void interestStudiesByConditionsAll(
        @PathVariable long userId,
        @RequestParam Gender gender,
        @RequestParam int minAge,
        @RequestParam int maxAge,
        @RequestParam boolean isOnline,
        @RequestParam boolean hasFee
    ) {
        // 메소드 구현
    }

    @GetMapping("/search/users/{userId}/interest-studies/conditions/specific")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] 특정 조건 + 내 '특정' 관심사 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 입력한 조건에 맞는 회원의 특정 관심 분야의 스터디를 조회 합니다.
            
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    @Parameter(name = "userId", description = "접속한 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "gender", description = "성별을 입력 받습니다.", required = false)
    @Parameter(name = "themeType", description = "관심분야를 입력 받습니다.", required = false)
    @Parameter(name = "minAge", description = "minAge는 18 이상의 정수 입니다.", required = false)
    @Parameter(name = "maxAge", description = "maxAge는 60 이하의 정수를 입력 받습니다.", required = false)
    @Parameter(name = "isOnline", description = "스터디 온라인 진행 여부 입니다. true, false의 bool 값으로 입력 받습니다", required = false)
    @Parameter(name = "hasFee", description = "스터디 활동비 유무 입니다. true, false의 bool 값으로 입력 받습니다", required = false)
    public void interestStudiesByConditionsSpecific(
        @PathVariable long userId,
        @RequestParam Gender gender,
        @RequestParam ThemeType themeType,
        @RequestParam int minAge,
        @RequestParam int maxAge,
        @RequestParam boolean isOnline,
        @RequestParam boolean hasFee
    ) {
        // 메소드 구현
    }





}

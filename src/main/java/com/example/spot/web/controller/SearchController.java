package com.example.spot.web.controller;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search", description = "Search API")
@RestController
@RequestMapping("/spot")
public class SearchController {

    @GetMapping("/search/user/{userId}/interest-studies/main")
    @Operation(summary = "[메인 화면 - 개발중] 회원 별 관심 분야 스터디 3개 조회",
        description = """
            ## [메인 화면] 접속한 회원의 관심 스터디 3개를 조회 합니다.
            조회된 스터디 3개의 정보가 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    public void interestStudiesForMain(@PathVariable @Schema(description = "접속한 유저의 ID를 입력 받습니다.", type = "long") long userId) {}

    @GetMapping("/search/user/{userId}/recommend-studies/main")
    @Operation(summary = "[메인 화면 - 개발중] 회원 별 추천 스터디 3개 조회",
        description = """
            ## [메인 화면] 접속한 회원의 추천 스터디 3개를 조회 합니다.
            조회된 스터디 3개의 정보가 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken"))
    public void recommendStudiesForMain(@PathVariable @Schema(description = "접속한 유저의 ID를 입력 받습니다.", type = "long")long userId) {}

    @GetMapping("/search/users/{userId}/interest-studies/all")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] '전체' 관심 분야 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 접속한 회원의 모든 관심 분야의 스터디를 전체 조회 합니다.
            
            조회된 전체 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    public void interestStudies(@PathVariable @Schema(description = "접속한 유저의 ID를 입력 받습니다.", type = "long")long userId) {
        // 메소드 구현
    }

    @GetMapping("/search/users/{userId}/interest-studies")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] '특정' 관심 분야 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 접속한 회원의 특정 관심 분야의 스터디를 조회 합니다.
            
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    public void interestStudiesByTopic(
        @PathVariable @Schema(description = "접속한 유저의 ID를 입력 받습니다.", type = "long") long userId,
        @RequestParam  ThemeType themeType
    ) {
        // 메소드 구현
    }

    @GetMapping("/search/users/{userId}/interest-studies/conditions")
    @Operation(
        summary = "[내 관심사 스터디 조회 - 개발중] 특정 조건 + 내 관심사 스터디 조회",
        description = """
            ## [내 관심사 스터디 조회] 입력한 조건에 맞는 접속한 회원의 특정 관심 분야의 스터디를 조회 합니다.
            
            조건에 맞게 검색된 스터디 목록이 반환 됩니다.""",
        security = @SecurityRequirement(name = "accessToken")
    )
    public void interestStudiesByConditions(
        @PathVariable @Schema(description = "접속한 유저의 ID를 입력 받습니다.", type = "long") long userId,
        @RequestParam Gender gender,
        @RequestParam @Schema(description = "minAge는 18 이상의 정수 입니다.", type = "int") int minAge,
        @RequestParam @Schema(description = "maxAge는 60 이하의 정수를 입력 받습니다.", type = "int") int maxAge,
        @RequestParam @Schema(description = "스터디 온라인 진행 여부 입니다. true, false의 bool 값으로 입력 받습니다", type = "bool") boolean isOnline,
        @RequestParam @Schema(description = "스터디 활동비 유무 입니다. true, false의 bool 값으로 입력 받습니다", type = "bool")boolean hasFee
    ) {
        // 메소드 구현
    }


}

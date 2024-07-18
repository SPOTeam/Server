package com.example.spot.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Admin API")
@RestController
@RequestMapping("/spot/admin")
public class AdminController {

    @Operation(
        summary = "[신고 내역 조회 - 개발중] 특정 스터디 신고 내역 조회",
        description = """
            ## [신고 내역 조회] 신고 내역을 조회하고 싶은 스터디의 ID를 입력합니다.
            해당 스터디에 대한 신고 내역이 반환 됩니다.
            """
    )
    @Parameter(name = "studyId", description = "조회할 스터디의 ID를 입력 받습니다.", required = true)
    @GetMapping("/reports/studies/{studyId}")
    public void getReportInStudy(@PathVariable long studyId, @RequestHeader String accessToken) {
        // 메소드 구현
    }

    @GetMapping("/reports/studies")
    @Operation(
        summary = "[신고 내역 조회 - 개발중] 전체 스터디 신고 내역 조회",
        description = """
            ## [신고 내역 조회] 현재 활동중인 모든 스터디에 대해 신고된 내역을 조회합니다.
            모든 스터디에 대한 신고 내역이 반환 됩니다.
            """
    )
    public void getAllReportsInStudies(@RequestHeader String accessToken) {
        // 메소드 구현
    }

    @GetMapping("/reports/posts/{postId}")
    @Operation(
        summary = "[신고 내역 조회 - 개발중] 특정 게시글 신고 내역 조회",
        description = """
            ## [신고 내역 조회] 신고 내역을 조회하고 싶은 게시글의 ID를 입력합니다.
            해당 게시글에 대한 신고 내역이 반환 됩니다.
            """
    )
    @Parameter(name = "postId", description = "조회할 게시글의 ID를 입력 받습니다.", required = true)
    public void getReportInPost(@PathVariable long postId, @RequestHeader String accessToken) {
        // 메소드 구현
    }

    @GetMapping("/reports/posts")
    @Operation(
        summary = "[신고 내역 조회 - 개발중] 전체 게시글 신고 내역 조회",
        description = """
            ## [신고 내역 조회] 현재 활성화 된 모든 게시글에 대해 신고된 내역을 조회합니다.
            모든 게시글에 대한 신고 내역이 반환 됩니다.
            """
    )
    public void getAllReportsInPosts(@RequestHeader String accessToken) {
        // 메소드 구현
    }


}

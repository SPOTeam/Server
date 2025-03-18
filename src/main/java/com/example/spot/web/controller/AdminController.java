package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.service.admin.AdminService;
import com.example.spot.web.dto.admin.AdminResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - 개발 중", description = "관리자 관련 API")
@RestController
@RequestMapping("/spot/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

/* ----------------------------- 회원 정보 관리 API ------------------------------------- */

    @Operation(summary = "[회원 정보 관리] 탈퇴 회원 영구 삭제 수동 실행 API",
            description = """
            ## [회원 정보 관리] inactive 시점 이후로 30일이 지난 회원의 정보를 DB에서 영구적으로 삭제합니다.
            * <탈퇴 회원 영구 삭제>는 스케줄러에 의해 오전 6시마다 실행되고 있습니다.
            * 오류로 인하여 수동 삭제가 필요한 경우 해당 API를 호출해주시기 바랍니다.
            """)
    @DeleteMapping("/members/removal")
    public ApiResponse<AdminResponseDTO.DeletedMemberListDTO> deleteInactiveMembers() {
        // 관리자 인증
        if (!adminService.getIsAdmin()) {
            throw new GeneralException(ErrorStatus._NOT_ADMIN);
        }
        AdminResponseDTO.DeletedMemberListDTO deletedMemberListDTO = adminService.deleteInactiveMembers();
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_DELETED, deletedMemberListDTO);
    }

/* ----------------------------- 신고 내역 관리 API ------------------------------------- */

    @Operation(
        summary = "[신고 내역 조회 - 개발중] 특정 스터디 신고 내역 조회",
        description = """
            ## [신고 내역 조회] 신고 내역을 조회하고 싶은 스터디의 ID를 입력합니다.
            해당 스터디에 대한 신고 내역이 반환 됩니다.
            """
    )
    @Parameter(name = "studyId", description = "조회할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "accessToken", description = "관리자의 accessToken을 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @GetMapping("/reports/studies/{studyId}")
    public void getReportInStudy(
        @PathVariable long studyId,
        @RequestHeader String accessToken,
        @RequestParam Integer page,
        @RequestParam Integer size
    ) {
        throw new GeneralException(ErrorStatus._BAD_VALUE_REQUEST);
        // 메소드 구현
    }

    @GetMapping("/reports/studies")
    @Operation(
        summary = "[신고 내역 조회 - 개발중] 전체 스터디 신고 내역 조회",
        description = """
            ## [신고 내역 조회] 현재 활동중인 모든 스터디에 대해 접수 된 신고 내역을 조회합니다.
            모든 스터디에 대한 신고 내역이 반환 됩니다.
            """
    )
    @Parameter(name = "accessToken", description = "관리자의 accessToken을 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public void getAllReportsInStudies(
        @RequestHeader String accessToken,
        @RequestParam Integer page,
        @RequestParam Integer size) {
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
    @Parameter(name = "accessToken", description = "관리자의 accessToken을 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public void getReportInPost(
        @PathVariable long postId,
        @RequestHeader String accessToken,
        @RequestParam Integer page,
        @RequestParam Integer size){
        // 메소드 구현
    }

    @GetMapping("/reports/posts")
    @Operation(
        summary = "[신고 내역 조회 - 개발중] 전체 게시글 신고 내역 조회",
        description = """
            ## [신고 내역 조회] 현재 활성화 된 모든 게시글에 대해 접수된 신고 내역을 조회합니다.
            모든 게시글에 대한 신고 내역이 반환 됩니다.
            """
    )
    @Parameter(name = "accessToken", description = "관리자의 accessToken을 입력 받습니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    public void getAllReportsInPosts(
        @RequestHeader String accessToken,
        @RequestParam Integer page,
        @RequestParam Integer size) {
        // 메소드 구현
    }

    @DeleteMapping("/reports/studies/{studyId}/members/{memberId}")
    @Operation(
        summary = "[신고 관리 - 개발중] 신고 당한 스터디 회원 강제 탈퇴 처리",
        description = """
            ## [신고 관리] 신고를 받은 스터디 회원을 검토하여 강제 탈퇴 처리 합니다.
            탈퇴 처리 성공 여부를 반환 합니다.
            """
    )
    @Parameter(name = "studyId", description = "신고를 받은 회원이 포함 된 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "memberId", description = "신고를 받은 스터디 회원의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "accessToken", description = "관리자의 accessToken을 입력 받습니다.", required = true)
    public void deleteMemberInReportedStudy(
        @PathVariable long studyId,
        @PathVariable long memberId,
        @RequestHeader String accessToken) {
        // 메소드 구현
    }

    @DeleteMapping("/reports/posts/{postId}")
    @Operation(
        summary = "[신고 관리 - 개발중] 신고 당한 게시글 삭제 처리",
        description = """
            ## [신고 관리] 신고를 받은 게시글을 검토하여 삭제 처리 합니다.
            삭제 처리 성공 여부를 반환 합니다.
            """
    )
    @Parameter(name = "postId", description = "신고를 받은 게시글의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "accessToken", description = "관리자의 accessToken을 입력 받습니다.", required = true)
    public void deletePost(@PathVariable long postId, @RequestHeader String accessToken) {
        // 메소드 구현
    }


}

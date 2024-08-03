package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.enums.Theme;
import com.example.spot.service.memberstudy.MemberStudyCommandService;
import com.example.spot.service.memberstudy.MemberStudyQueryService;
import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.validation.annotation.ExistStudy;
import com.example.spot.web.dto.memberstudy.request.*;
import com.example.spot.web.dto.memberstudy.response.*;
import com.example.spot.web.dto.study.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MemberStudy", description = "MemberStudy API(내 스터디 관련 API)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
@Validated
public class MemberStudyController {

    private final MemberStudyQueryService memberStudyQueryService;
    private final MemberStudyCommandService memberStudyCommandService;

/* ----------------------------- 진행중인 스터디 관련 API ------------------------------------- */


    @Operation(summary = "[진행중인 스터디] 스터디 탈퇴하기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 현재 진행중인 스터디에서 탈퇴합니다.
        로그인한 회원이 참여하는 특정 스터디에 대해 member_study 튜플을 삭제합니다.
        """)
    @DeleteMapping("/members/{memberId}/studies/{studyId}/withdrawal")
    public ApiResponse<StudyWithdrawalResponseDTO.WithdrawalDTO> withdrawFromStudy(@PathVariable Long memberId, @PathVariable Long studyId) {
        StudyWithdrawalResponseDTO.WithdrawalDTO withdrawalDTO = memberStudyCommandService.withdrawFromStudy(memberId, studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_DELETED, withdrawalDTO);
    }

    @Operation(summary = "[진행중인 스터디] 스터디 끝내기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 운영중인 스터디를 끝냅니다.
        로그인한 회원이 운영하는 특정 스터디에 대해 study status OFF로 전환합니다.
        """)
    @PatchMapping("/studies/{studyId}/termination")
    public ApiResponse<StudyTerminationResponseDTO.TerminationDTO> terminateStudy(@PathVariable Long studyId) {
        StudyTerminationResponseDTO.TerminationDTO terminationDTO = memberStudyCommandService.terminateStudy(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_TERMINATED, terminationDTO);
    }

//    @Operation(summary = "[데모 데이 이후 개발 -> 진행중인 스터디] 스터디 정보 수정하기", description = """
//        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 운영중인 스터디의 정보를 수정합니다.
//        로그인한 회원이 운영하는 특정 스터디에 대해 study 정보를 수정합니다.
//        """)
//    @PatchMapping("/studies/{studyId}")
//    public void updateStudy(@PathVariable @ExistStudy Long studyId) {
//    }

/* ----------------------------- 모집중인 스터디 관련 API ------------------------------------- */


    @Operation(summary = "[모집중인 스터디] 스터디별 신청 회원 목록 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원 목록을 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 대해 member_study의 application_status가 APPLIED인 회원 목록이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyMemberResponseDTO> getAllApplicants(@PathVariable @ExistStudy Long studyId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_FOUND,
            memberStudyQueryService.findStudyApplicants(studyId));
    }

    @Operation(summary = "[모집중인 스터디] 스터디 신청 정보(이름, 자기소개) 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원의 정보를 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 신청한 회원의 정보(member.name & member_study.introduction)가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants/{applicantId}")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "applicantId", description = "신청자의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyMemberResponseDTO.StudyApplyMemberDTO> getApplicantInfo(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long applicantId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_FOUND,
            memberStudyQueryService.findStudyApplication(studyId, applicantId));
    }

    @Operation(summary = "[모집중인 스터디] 스터디 신청 처리하기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 > 거절 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원을 처리합니다.
        isAccept가 true인 경우 member_study에서 application_status를 APPROVE로 수정합니다.
        isAccept가 false인 경우 member_study에서 application_status를 REJECTED로 수정합니다.
        스터디 신청 처리 결과를 응답으로 반환합니다. 
        """)
    @PostMapping("/studies/{studyId}/applicants/{applicantId}")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "applicantId", description = "신청자의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyApplyResponseDTO> rejectApplicant(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long applicantId,
        @RequestParam boolean isAccept) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_UPDATED,
            memberStudyCommandService.acceptAndRejectStudyApply(applicantId, studyId, isAccept));
    }


/* ----------------------------- 스터디 상세 정보 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 상세 정보] 스터디 최근 공지 1개 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 최근 공지 1개를 불러옵니다.
        study_post의 announced_at이 가장 최근인 공지 1개가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/announce")
    public ApiResponse<StudyPostResponseDTO> getRecentAnnouncement(@PathVariable @ExistStudy Long studyId) {
        StudyPostResponseDTO studyPostResponseDTO = memberStudyQueryService.findStudyAnnouncementPost(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_FOUND, studyPostResponseDTO);
    }

    @Operation(summary = "[스터디 상세 정보] 다가오는 모임 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 다가오는 모임 목록을 페이징 조회 합니다.
        현재 시점 이후에 진행되는 모임 일정의 목록을 schedule에서 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/upcoming-schedules")
    public ApiResponse<StudyScheduleResponseDTO> getUpcomingSchedules(
        @PathVariable @ExistStudy Long studyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "1") int size){
        StudyScheduleResponseDTO studyScheduleResponseDTO = memberStudyQueryService.findStudySchedule(studyId, PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_FOUND, studyScheduleResponseDTO);
    }

    @Operation(summary = "[스터디 상세 정보] 스터디에 참여하는 회원 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 로그인한 회원이 참여하는 특정 스터디의 회원 목록을 전체 합니다.
        member_study에서 application_status=APPROVED인 회원의 목록(이름, 프로필 사진 포함)이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/members")
    public ApiResponse<StudyMemberResponseDTO> getStudyMembers(
        @PathVariable @ExistStudy Long studyId){
        StudyMemberResponseDTO studyMemberResponseDTO = memberStudyQueryService.findStudyMembers(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_FOUND, studyMemberResponseDTO);
    }


/* ----------------------------- 스터디 일정 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 일정] 월별 일정 불러오기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 클릭, 로그인한 회원이 참여하는 특정 스터디의 일정을 월 단위로 불러옵니다.
        처음 캘린더를 클릭하면 오늘 날짜가 포함된 연/월에 해당하는 일정 목록이 schedule에서 반환됩니다.
        캘린더를 넘기면 해당 연/월에 해당하는 일정 목록이 schedule에서 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/schedules")
    public ApiResponse<ScheduleResponseDTO.MonthlyScheduleListDTO> getMonthlySchedules(@PathVariable Long studyId, @RequestParam Integer year, @RequestParam Integer month) {
        ScheduleResponseDTO.MonthlyScheduleListDTO monthlyScheduleDTO = memberStudyQueryService.getMonthlySchedules(studyId, year, month);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_FOUND, monthlyScheduleDTO);
    }

    @Operation(summary = "[스터디 일정] 상세 일정 불러오기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 > 일정 클릭, 로그인한 회원이 참여하는 특정 스터디의 상세 일정을 불러옵니다.
        스터디의 일정 정보를 상세하게 불러옵니다.
        """)
    @GetMapping("/studies/{studyId}/schedules/{scheduleId}")
    public ApiResponse<ScheduleResponseDTO.MonthlyScheduleDTO> getSchedule(@PathVariable Long studyId, @PathVariable Long scheduleId) {
        ScheduleResponseDTO.MonthlyScheduleDTO scheduleDTO = memberStudyQueryService.getSchedule(studyId, scheduleId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_FOUND, scheduleDTO);
    }

    @Operation(summary = "[스터디 일정] 일정 추가하기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 > 추가 버튼 클릭, 로그인한 회원이 운영하는 특정 스터디에 일정을 추가합니다.
        로그인한 회원이 owner인 경우 schedule에 새로운 일정을 등록합니다.
        """)
    @PostMapping("/studies/{studyId}/schedules")
    public ApiResponse<ScheduleResponseDTO.ScheduleDTO> addSchedule(@PathVariable Long studyId, @RequestBody ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO) {
        ScheduleResponseDTO.ScheduleDTO scheduleResponseDTO = memberStudyCommandService.addSchedule(studyId, scheduleRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_CREATED, scheduleResponseDTO);
    }

    @Operation(summary = "[스터디 일정] 일정 변경하기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 > 일정 클릭, 로그인한 회원이 특정 스터디에 등록한 일정을 수정합니다.
        로그인한 회원이 owner인 경우 schedule에 등록한 일정을 수정할 수 있습니다.
        """)
    @PatchMapping("/studies/{studyId}/schedules/{scheduleId}")
    public ApiResponse<ScheduleResponseDTO.ScheduleDTO> modSchedule(@PathVariable Long studyId, @PathVariable Long scheduleId, @RequestBody ScheduleRequestDTO.ScheduleDTO scheduleModDTO) {
        ScheduleResponseDTO.ScheduleDTO scheduleResponseDTO = memberStudyCommandService.modSchedule(studyId, scheduleId, scheduleModDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_UPDATED, scheduleResponseDTO);
    }

/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 게시글] 게시글 작성하기", description = """
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 작성 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 새로운 게시글을 등록합니다.
        스터디에 참여하는 회원이 작성한 게시글을 `study_post`에 저장합니다.
        """)
    @PostMapping(value = "/studies/{studyId}/posts", consumes = "multipart/form-data")
    public ApiResponse<StudyPostResDTO.PostPreviewDTO> createPost(
            @PathVariable Long studyId,
            @ModelAttribute(value = "post") @Parameter(content = @Content(mediaType = "multipart/form-data")) StudyPostRequestDTO.PostDTO postRequestDTO) {

        StudyPostResDTO.PostPreviewDTO postPreviewDTO = memberStudyCommandService.createPost(studyId, postRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_CREATED, postPreviewDTO);
    }

    @Operation(summary = "[스터디 게시글] 게시글 삭제하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디에서 작성한 게시글을 삭제합니다.
        스터디에 참여하는 회원이 작성한 게시글을 study_post에서 삭제합니다.
        게시글에 작성된 댓글도 함께 삭제됩니다.
        """)
    @DeleteMapping("/studies/{studyId}/posts/{postId}")
    public ApiResponse<StudyPostResDTO.PostPreviewDTO> deletePost(@PathVariable Long studyId, @PathVariable Long postId) {
        StudyPostResDTO.PostPreviewDTO postPreviewDTO = memberStudyCommandService.deletePost(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_DELETED, postPreviewDTO);
    }

    @Operation(summary = "[스터디 게시글] 글 목록 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글 목록을 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post 목록이 최신순으로 반환됩니다.
        query를 추가하는 경우 해당 카테고리에 속한 스터디 게시글 목록을 반환합니다.
        (* 페이징 필요)
        """)
    @GetMapping("/studies/{studyId}/posts")
    public ApiResponse<StudyPostResDTO.PostListDTO> getAllPosts(@PathVariable Long studyId, @RequestParam(required = false) Theme theme,
                                                                @RequestParam @Min(0) Integer offset, @RequestParam @Min(1) Integer limit) {
        StudyPostResDTO.PostListDTO postListDTO = memberStudyQueryService.getAllPosts(PageRequest.of(offset, limit), studyId, theme);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_LIST_FOUND, postListDTO);
    }

    @Operation(summary = "[스터디 게시글] 게시글 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글을 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post 정보가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/posts/{postId}")
    public ApiResponse<StudyPostResDTO.PostDetailDTO> getPost(@PathVariable Long studyId, @PathVariable Long postId) {

        StudyPostResDTO.PostDetailDTO postDetailDTO = memberStudyQueryService.getPost(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_FOUND, postDetailDTO);
    }

    @Operation(summary = "[스터디 게시글] 좋아요 누르기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 좋아요를 누릅니다.
        study_liked_post에 좋아요를 누른 회원의 정보를 저장하고 게시글의 like_num을 업데이트합니다.
        ** 인증 구현 이후 /members/{memberId} 삭제 **
        """)
    @PostMapping("/studies/{studyId}/posts/{postId}/likes/members/{memberId}")
    public ApiResponse<StudyPostResDTO.PostLikeNumDTO> likePost(@PathVariable Long studyId, @PathVariable Long postId, @PathVariable Long memberId) {
        StudyPostResDTO.PostLikeNumDTO postLikeNumDTO = memberStudyCommandService.likePost(studyId, postId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_LIKED, postLikeNumDTO);
    }

    @Operation(summary = "[스터디 게시글] 좋아요 취소하기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 좋아요를 취소합니다.
        study_liked_post에 좋아요를 누른 회원의 정보를 저장하고 게시글의 like_num을 업데이트합니다.
        ** 인증 구현 이후 /members/{memberId} 삭제 **
        """)
    @DeleteMapping("/studies/{studyId}/posts/{postId}/likes/members/{memberId}")
    public ApiResponse<StudyPostResDTO.PostLikeNumDTO> cancelPostLike(@PathVariable Long studyId, @PathVariable Long postId, @PathVariable Long memberId) {
        StudyPostResDTO.PostLikeNumDTO postLikeNumDTO = memberStudyCommandService.cancelPostLike(studyId, postId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_DISLIKED, postLikeNumDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 작성하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디의 게시글에 댓글을 작성합니다.
        RequestBody로 내용과 회원 정보를 입력 받아 댓글 정보를 반환합니다.
        """)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments")
    public ApiResponse<StudyPostCommentResponseDTO.CommentDTO> createComment(@PathVariable Long studyId, @PathVariable Long postId,
                                                                             @RequestBody StudyPostCommentRequestDTO.CommentDTO commentRequestDTO) {
        StudyPostCommentResponseDTO.CommentDTO commentResponseDTO = memberStudyCommandService.createComment(studyId, postId, commentRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_CREATED, commentResponseDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 답글 작성하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글에 대하여 답글을 작성합니다.
        RequestBody로 내용과 회원 정보를 입력 받아 답글 정보를 반환합니다.
        """)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/replies")
    public ApiResponse<StudyPostCommentResponseDTO.CommentDTO> createReply(@PathVariable Long studyId, @PathVariable Long postId, @PathVariable Long commentId,
                                                                           @RequestBody StudyPostCommentRequestDTO.CommentDTO commentRequestDTO) {
        StudyPostCommentResponseDTO.CommentDTO commentResponseDTO = memberStudyCommandService.createReply(studyId, postId, commentId, commentRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_CREATED, commentResponseDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 삭제하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글을 삭제합니다.
        댓글의 id를 PathVariable로 받아 content와 isDeleted를 수정합니다.
        """)
    @PatchMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}")
    public ApiResponse<StudyPostCommentResponseDTO.CommentIdDTO> deleteComment(@PathVariable Long studyId, @PathVariable Long postId, @PathVariable Long commentId) {
        StudyPostCommentResponseDTO.CommentIdDTO commentPreviewDTO = memberStudyCommandService.deleteComment(studyId, postId, commentId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_DELETED, commentPreviewDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 좋아요 누르기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글에 좋아요를 누릅니다.
        study_liked_comment에 좋아요 내역이 추가되고 study_post_comment의 like_count가 증가합니다.
        ** 인증 구현 이후 RequestParam 삭제 **
        """)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/likes")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> likeComment(
            @PathVariable Long studyId, @PathVariable Long postId,
            @PathVariable Long commentId, @RequestParam Long memberId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = memberStudyCommandService.likeComment(studyId, postId, commentId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_LIKED, commentPreviewDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 싫어요 누르기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글에 싫어요를 누릅니다.
        study_liked_comment에 싫어요 내역이 추가되고 study_post_comment의 dislike_count가 증가합니다.
        ** 인증 구현 이후 RequestParam 삭제 **
        """)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/dislikes")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> dislikeComment(
            @PathVariable Long studyId, @PathVariable Long postId,
            @PathVariable Long commentId, @RequestParam Long memberId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = memberStudyCommandService.dislikeComment(studyId, postId, commentId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_DISLIKED, commentPreviewDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 좋아요 취소하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글 댓글에 달린 좋아요를 취소합니다.
        study_liked_comment에서 좋아요 내역이 삭제되고 study_post_comment의 like_count가 감소합니다.
        ** 인증 구현 이후 RequestParam 삭제 **
        """)
    @DeleteMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/likes/{likeId}")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> cancelCommentLike(
            @PathVariable Long studyId, @PathVariable Long postId,
            @PathVariable Long commentId, @PathVariable Long likeId, @RequestParam Long memberId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = memberStudyCommandService.cancelCommentLike(studyId, postId, commentId, likeId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_LIKE_CANCELED, commentPreviewDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 싫어요 취소하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글 댓글에 달린 싫어요를 취소합니다.
        study_liked_comment에서 싫어요 내역이 삭제되고 study_post_comment의 dislike_count가 감소합니다.
        ** 인증 구현 이후 RequestParam 삭제 **
        """)
    @DeleteMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/dislikes/{dislikeId}")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> cancelCommentDislike(
            @PathVariable Long studyId, @PathVariable Long postId,
            @PathVariable Long commentId, @PathVariable Long dislikeId, @RequestParam Long memberId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = memberStudyCommandService.cancelCommentDislike(studyId, postId, commentId, dislikeId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_DISLIKE_CANCELED, commentPreviewDTO);
    }

    @Operation(summary = "[스터디 게시글 - 댓글] 전체 댓글 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 달린 모든 댓글을 불러옵니다.
        특정 study_post에 대한 comment(댓/답글) 목록이 반환됩니다.
        ** 인증 구현 이후 Request Body 삭제 **
        """)
    @GetMapping("/studies/{studyId}/posts/{postId}/comments")
    public ApiResponse<StudyPostCommentResponseDTO.CommentReplyListDTO> getAllComments(@PathVariable Long studyId, @PathVariable Long postId) {
        StudyPostCommentResponseDTO.CommentReplyListDTO commentReplyListDTO = memberStudyQueryService.getAllComments(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_FOUND, commentReplyListDTO);
    }


/* ----------------------------- 스터디 투표 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 투표] 투표 생성하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 작성 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 새로운 투표를 등록합니다.
        스터디에 참여하는 회원이 생성한 투표를 vote에 저장합니다.
        """)
    @PostMapping("/studies/{studyId}/votes")
    public ApiResponse<StudyVoteResponseDTO.VotePreviewDTO> createVote(@PathVariable Long studyId, @RequestBody StudyVoteRequestDTO.VoteDTO voteDTO) {
        StudyVoteResponseDTO.VotePreviewDTO votePreviewDTO = memberStudyCommandService.createVote(studyId, voteDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_CREATED, votePreviewDTO);
    }

    @Operation(summary = "[스터디 투표] 투표하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 특정 투표 클릭, 로그인한 회원이 참여하는 스터디에서 특정 항목에 투표합니다.
        member_vote에 투표 정보를 저장합니다.
        """)
    @PostMapping("/studies/{studyId}/votes/{voteId}/options")
    public ApiResponse<StudyVoteResponseDTO.VotedOptionDTO> vote(@PathVariable Long studyId, @PathVariable Long voteId, @RequestBody StudyVoteRequestDTO.VotedOptionDTO votedOptionDTO) {
        StudyVoteResponseDTO.VotedOptionDTO votedOptionResDTO = memberStudyCommandService.vote(studyId, voteId, votedOptionDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_PARTICIPATED, votedOptionResDTO);
    }

    @Operation(summary = "[스터디 투표] 투표 편집하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 편집하기 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 투표 정보를 수정합니다.
        스터디에 참여하는 회원이 생성한 투표를 vote에 저장합니다.
        """)
    @PatchMapping("/studies/{studyId}/votes/{voteId}")
    public ApiResponse<StudyVoteResponseDTO.VotePreviewDTO> updateVote(
            @PathVariable Long studyId, @PathVariable Long voteId, @RequestBody StudyVoteRequestDTO.VoteUpdateDTO voteDTO) {
        StudyVoteResponseDTO.VotePreviewDTO votePreviewDTO = memberStudyCommandService.updateVote(studyId, voteId, voteDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_UPDATED, votePreviewDTO);
    }

    @Operation(summary = "[스터디 투표] 투표 삭제하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 삭제하기 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 투표를 삭제합니다.
        스터디에 참여하는 회원이 생성한 투표를 vote에 저장합니다.
        """)
    @DeleteMapping("/studies/{studyId}/votes/{voteId}")
    public ApiResponse<StudyVoteResponseDTO.VotePreviewDTO> deleteVote(@PathVariable Long studyId, @PathVariable Long voteId) {
        StudyVoteResponseDTO.VotePreviewDTO votePreviewDTO = memberStudyCommandService.deleteVote(studyId, voteId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_DELETED, votePreviewDTO);
    }

    @Operation(summary = "[스터디 투표] 투표 목록 불러오기", description = """
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표 목록을 불러옵니다.
        진행 중(finished_at 이전)인 투표 목록과 마감(finished_at 이후)된 투표 목록을 구분하여 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/votes")
    public ApiResponse<StudyVoteResponseDTO.VoteListDTO> getAllVotes(@PathVariable Long studyId) {
        StudyVoteResponseDTO.VoteListDTO voteListDTO = memberStudyQueryService.getAllVotes(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_FOUND, voteListDTO);
    }

    @Operation(summary = "[스터디 투표] 투표 불러오기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 특정 투표 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표를 불러옵니다.
        진행중인 투표 : 진행중인 투표에 대한 항목 및 기본 정보가 반환됩니다.
        마감된 투표 : 마감된 투표에 대한 항목과 투표 인원수가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/votes/{voteId}")
    public ApiResponse<?> getVote(@PathVariable Long studyId, @PathVariable Long voteId) {
        // 진행중인 투표 : return VoteDTO
        // 마감된 투표 : return CompletedVoteDTO
        Boolean isCompleted = memberStudyQueryService.getIsCompleted(voteId);
        if (isCompleted) {
            StudyVoteResponseDTO.CompletedVoteDTO completedVoteDTO = memberStudyQueryService.getVoteInCompletion(studyId, voteId);
            return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_FOUND, completedVoteDTO);
        } else {
            StudyVoteResponseDTO.VoteDTO voteDTO = memberStudyQueryService.getVoteInProgress(studyId, voteId);
            return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_FOUND, voteDTO);
        }
    }

    @Operation(summary = "[스터디 투표] 마감된 투표 현황 불러오기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 마감된 투표 > n명 참여 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표를 불러옵니다.
        마감된 투표에 대하여 항목별 투표 회원 목록을 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/votes/{voteId}/details")
    public ApiResponse<StudyVoteResponseDTO.CompletedVoteDetailDTO> getCompletedVoteDetail(@PathVariable Long studyId, @PathVariable Long voteId) {
        StudyVoteResponseDTO.CompletedVoteDetailDTO completedVoteDetailDTO = memberStudyQueryService.getCompletedVoteDetail(studyId, voteId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_DETAIL_STATUS_FOUND, completedVoteDetailDTO);
    }

/* ----------------------------- 스터디 갤러리 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 갤러리] 스터디 이미지 목록 불러오기", description = """ 
        ## [스터디 갤러리] 내 스터디 > 스터디 > 갤러리 클릭, 로그인한 회원이 참여하는 스터디의 이미지 목록을 불러옵니다.
        study_post에 존재하는 모든 게시글의 이미지를 최신순으로 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/images")
    public void getAllStudyImages(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 갤러리] 스터디 이미지 불러오기", description = """ 
        ## [스터디 갤러리] 내 스터디 > 스터디 > 갤러리 > 이미지 클릭, 로그인한 회원이 참여하는 스터디의 특정 이미지를 불러옵니다.
        특정 study_post의 image 하나를 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/images/{imageId}")
    public void getStudyImage(@PathVariable Long studyId, @PathVariable Long imageId) {
    }

/* ----------------------------- 스터디 출석체크 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 출석체크] 출석 퀴즈 생성하기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 출석체크 > 퀴즈 만들기 클릭, 로그인한 회원이 운영하는 스터디에 퀴즈를 생성합니다.
        로그인한 회원이 스터디장인 경우 quiz에 새로운 퀴즈를 생성합니다.
        """)
    @PostMapping("/studies/{studyId}/quizzes")
    public ApiResponse<StudyQuizResponseDTO.QuizDTO> createAttendanceQuiz(@PathVariable Long studyId, @RequestBody StudyQuizRequestDTO.QuizDTO quizRequestDTO) {
        StudyQuizResponseDTO.QuizDTO quizResponseDTO = memberStudyCommandService.createAttendanceQuiz(studyId, quizRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_QUIZ_CREATED, quizResponseDTO);
    }

    @Operation(summary = "[스터디 출석체크] 출석 체크하기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 이미지 클릭, 로그인한 회원이 참여하는 스터디에서 오늘의 퀴즈를 풀어 출석을 체크합니다.
        특정 시점의 quiz에 대해 member_attendance 튜플을 추가합니다.
        """)
    @PostMapping("/studies/{studyId}/quizzes/{quizId}")
    public ApiResponse<StudyQuizResponseDTO.AttendanceDTO> attendantStudy(@PathVariable Long studyId, @PathVariable Long quizId,
                                        @RequestBody StudyQuizRequestDTO.AttendanceDTO attendanceRequestDTO) {
        StudyQuizResponseDTO.AttendanceDTO attendanceResponseDTO = memberStudyCommandService.attendantStudy(studyId, quizId, attendanceRequestDTO);
        if (attendanceResponseDTO.getIsCorrect()) {
            return ApiResponse.onSuccess(SuccessStatus._STUDY_ATTENDANCE_CREATED_CORRECT_ANSWER, attendanceResponseDTO);
        } else {
            return ApiResponse.onSuccess(SuccessStatus._STUDY_ATTENDANCE_CREATED_WRONG_ANSWER, attendanceResponseDTO);
        }
    }

    @Operation(summary = "[스터디 출석체크] 출석 퀴즈 삭제하기", description = """ 
        ## [스터디 출석체크] 기한이 지난 출석 퀴즈를 삭제합니다. (화면 X)
        PathVariable을 통해 전달받은 정보를 바탕으로 출석 퀴즈를 삭제합니다.
        출석 퀴즈 정보와 함께 퀴즈에 대한 MemberAttendance(회원 출석) 목록도 함께 삭제됩니다.
        """)
    @DeleteMapping("/studies/{studyId}/quizzes/{quizId}")
    public ApiResponse<StudyQuizResponseDTO.QuizDTO> deleteAttendanceQuiz(@PathVariable Long studyId, @PathVariable Long quizId) {
        StudyQuizResponseDTO.QuizDTO quizDTO = memberStudyCommandService.deleteAttendanceQuiz(studyId, quizId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_QUIZ_DELETED, quizDTO);
    }

    @Operation(summary = "[스터디 출석체크] 금일 회원 출석부 불러오기", description = """ 
        ## [스터디 출석체크] 금일 모든 스터디 회원의 출석 여부를 불러옵니다.
        출석체크 화면에 표시되는 스터디 회원 정보(프로필 사진, 이름, 출석 여부, 스터디장 여부) 목록를 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/quizzes/{quizId}/members")
    public ApiResponse<StudyQuizResponseDTO.AttendanceListDTO> getAllAttendances(@PathVariable Long studyId, @PathVariable Long quizId) {
        StudyQuizResponseDTO.AttendanceListDTO attendanceListDTO = memberStudyQueryService.getAllAttendances(studyId, quizId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_ATTENDANCES_FOUND, attendanceListDTO);
    }



/* ----------------------------- 스터디 회원 신고 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 회원 신고] 스터디원 신고하기", description = """ 
        ## [스터디 회원 신고] 로그인한 회원이 참여하는 스터디의 다른 회원을 신고합니다.
        member_report에 피신고자의 member_id를 포함하여 새로운 튜플을 추가합니다.
        """)
    @PostMapping("/studies/{studyId}/members/{memberId}")
    public void reportStudyMember(@PathVariable Long memberId, @PathVariable Long studyId) {
    }
}

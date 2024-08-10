package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.enums.ThemeQuery;
import com.example.spot.service.studypost.StudyPostCommandService;
import com.example.spot.service.studypost.StudyPostQueryService;
import com.example.spot.validation.annotation.ExistStudy;
import com.example.spot.validation.annotation.ExistStudyLikedComment;
import com.example.spot.validation.annotation.ExistStudyPost;
import com.example.spot.validation.annotation.ExistStudyPostComment;
import com.example.spot.web.dto.memberstudy.request.StudyPostCommentRequestDTO;
import com.example.spot.web.dto.memberstudy.request.StudyPostRequestDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostCommentResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
@Validated
public class StudyPostController {

    private final StudyPostQueryService studyPostQueryService;
    private final StudyPostCommandService studyPostCommandService;

/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    @Tag(name = "스터디 게시글")
    @Operation(summary = "[스터디 게시글] 게시글 작성하기", description = """
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 작성 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 새로운 게시글을 등록합니다.
        스터디에 참여하는 회원이 작성한 게시글을 `study_post`에 저장합니다.
        """)
    @Parameter(name = "studyId", description = "게시글을 작성할 스터디의 id를 입력합니다.", required = true)
    @PostMapping(value = "/studies/{studyId}/posts", consumes = "multipart/form-data")
    public ApiResponse<StudyPostResDTO.PostPreviewDTO> createPost(
            @PathVariable @ExistStudy Long studyId,
            @ModelAttribute(value = "post") @Parameter(content = @Content(mediaType = "multipart/form-data")) @Valid StudyPostRequestDTO.PostDTO postRequestDTO) {
        StudyPostResDTO.PostPreviewDTO postPreviewDTO = studyPostCommandService.createPost(studyId, postRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_CREATED, postPreviewDTO);
    }

    @Tag(name = "스터디 게시글")
    @Operation(summary = "[스터디 게시글] 게시글 삭제하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디에서 작성한 게시글을 삭제합니다.
        스터디에 참여하는 회원이 작성한 게시글을 study_post에서 삭제합니다.
        게시글에 작성된 댓글도 함께 삭제됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "삭제할 스터디 게시글의 id를 입력합니다.", required = true)
    @DeleteMapping("/studies/{studyId}/posts/{postId}")
    public ApiResponse<StudyPostResDTO.PostPreviewDTO> deletePost(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId) {
        StudyPostResDTO.PostPreviewDTO postPreviewDTO = studyPostCommandService.deletePost(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_DELETED, postPreviewDTO);
    }

    @Tag(name = "스터디 게시글")
    @Operation(summary = "[스터디 게시글] 글 목록 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글 목록을 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post 목록이 최신순으로 반환됩니다.
        
        query를 추가하는 경우 해당 카테고리에 속한 스터디 게시글 목록을 반환하며 query가 없는 경우 전체 게시글 목록을 반환합니다.
        
        themeQuery에는 [ANNOUNCEMENT, WELCOME, INFO_SHARING, STUDY_REVIEW, FREE_TALK, QNA] 중 하나를 입력해야 합니다.
        """)
    @Parameter(name = "studyId", description = "게시글 목록을 불러올 스터디의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/posts")
    public ApiResponse<StudyPostResDTO.PostListDTO> getAllPosts(
            @PathVariable @ExistStudy Long studyId,
            @RequestParam(required = false) ThemeQuery themeQuery,
            @RequestParam @Min(0) Integer offset,
            @RequestParam @Min(1) Integer limit) {
        StudyPostResDTO.PostListDTO postListDTO = studyPostQueryService.getAllPosts(PageRequest.of(offset, limit), studyId, themeQuery);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_LIST_FOUND, postListDTO);
    }

    @Tag(name = "스터디 게시글")
    @Operation(summary = "[스터디 게시글] 게시글 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글을 불러옵니다.
        로그인한 회원이 참여하는 특정 스터디의 study_post 정보가 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "불러올 스터디 게시글의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/posts/{postId}")
    public ApiResponse<StudyPostResDTO.PostDetailDTO> getPost(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId) {
        StudyPostResDTO.PostDetailDTO postDetailDTO = studyPostQueryService.getPost(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_FOUND, postDetailDTO);
    }

    @Tag(name = "스터디 게시글")
    @Operation(summary = "[스터디 게시글] 좋아요 누르기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 좋아요를 누릅니다.
        study_liked_post에 좋아요를 누른 회원의 정보를 저장하고 게시글의 like_num을 업데이트합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "좋아요를 누를 스터디 게시글의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/posts/{postId}/likes")
    public ApiResponse<StudyPostResDTO.PostLikeNumDTO> likePost(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId) {
        StudyPostResDTO.PostLikeNumDTO postLikeNumDTO = studyPostCommandService.likePost(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_LIKED, postLikeNumDTO);
    }

    @Tag(name = "스터디 게시글")
    @Operation(summary = "[스터디 게시글] 좋아요 취소하기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 좋아요를 취소합니다.
        study_liked_post에 좋아요를 누른 회원의 정보를 저장하고 게시글의 like_num을 업데이트합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "좋아요를 취소할 스터디 게시글의 id를 입력합니다.", required = true)
    @DeleteMapping("/studies/{studyId}/posts/{postId}/likes")
    public ApiResponse<StudyPostResDTO.PostLikeNumDTO> cancelPostLike(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId) {
        StudyPostResDTO.PostLikeNumDTO postLikeNumDTO = studyPostCommandService.cancelPostLike(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_DISLIKED, postLikeNumDTO);
    }

/* ----------------------------- 스터디 게시글 댓글 관련 API ------------------------------------- */

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 작성하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디의 게시글에 댓글을 작성합니다.
        RequestBody로 내용과 회원 정보를 입력 받아 댓글 정보를 반환합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "댓글을 작성할 스터디 게시글의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments")
    public ApiResponse<StudyPostCommentResponseDTO.CommentDTO> createComment(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId,
            @RequestBody @Valid StudyPostCommentRequestDTO.CommentDTO commentRequestDTO) {
        StudyPostCommentResponseDTO.CommentDTO commentResponseDTO = studyPostCommandService.createComment(studyId, postId, commentRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_CREATED, commentResponseDTO);
    }

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 답글 작성하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글에 대하여 답글을 작성합니다.
        RequestBody로 내용과 회원 정보를 입력 받아 답글 정보를 반환합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "스터디 게시글의 id를 입력합니다.", required = true)
    @Parameter(name = "commentId", description = "답글을 작성할 댓글의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/replies")
    public ApiResponse<StudyPostCommentResponseDTO.CommentDTO> createReply(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId,
            @PathVariable @ExistStudyPostComment Long commentId,
            @RequestBody @Valid StudyPostCommentRequestDTO.CommentDTO commentRequestDTO) {
        StudyPostCommentResponseDTO.CommentDTO commentResponseDTO = studyPostCommandService.createReply(studyId, postId, commentId, commentRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_CREATED, commentResponseDTO);
    }

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 삭제하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글을 삭제합니다.
        댓글의 id를 PathVariable로 받아 content와 isDeleted를 수정합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "스터디 게시글의 id를 입력합니다.", required = true)
    @Parameter(name = "commentId", description = "삭제할 댓글의 id를 입력합니다.", required = true)
    @PatchMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}")
    public ApiResponse<StudyPostCommentResponseDTO.CommentIdDTO> deleteComment(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId,
            @PathVariable @ExistStudyPostComment Long commentId) {
        StudyPostCommentResponseDTO.CommentIdDTO commentPreviewDTO = studyPostCommandService.deleteComment(studyId, postId, commentId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_DELETED, commentPreviewDTO);
    }

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 좋아요 누르기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글에 좋아요를 누릅니다.
        study_liked_comment에 좋아요 내역이 추가되고 study_post_comment의 like_count가 증가합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "스터디 게시글의 id를 입력합니다.", required = true)
    @Parameter(name = "commentId", description = "좋아요를 누를 댓글의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/likes")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> likeComment(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId,
            @PathVariable @ExistStudyPostComment Long commentId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = studyPostCommandService.likeComment(studyId, postId, commentId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_LIKED, commentPreviewDTO);
    }

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 싫어요 누르기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글의 댓글에 싫어요를 누릅니다.
        study_liked_comment에 싫어요 내역이 추가되고 study_post_comment의 dislike_count가 증가합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "스터디 게시글의 id를 입력합니다.", required = true)
    @Parameter(name = "commentId", description = "싫어요를 누를 댓글의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/dislikes")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> dislikeComment(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId,
            @PathVariable @ExistStudyPostComment Long commentId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = studyPostCommandService.dislikeComment(studyId, postId, commentId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_DISLIKED, commentPreviewDTO);
    }

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 좋아요 취소하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글 댓글에 달린 좋아요를 취소합니다.
        study_liked_comment에서 좋아요 내역이 삭제되고 study_post_comment의 like_count가 감소합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "스터디 게시글의 id를 입력합니다.", required = true)
    @Parameter(name = "commentId", description = "좋아요를 취소할 댓글의 id를 입력합니다.", required = true)
    @DeleteMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/likes")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> cancelCommentLike(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId,
            @PathVariable @ExistStudyPostComment Long commentId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = studyPostCommandService.cancelCommentLike(studyId, postId, commentId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_LIKE_CANCELED, commentPreviewDTO);
    }

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 댓글 싫어요 취소하기", description = """ 
        ## [스터디 게시글] 로그인한 회원이 참여하는 특정 스터디 게시글 댓글에 달린 싫어요를 취소합니다.
        study_liked_comment에서 싫어요 내역이 삭제되고 study_post_comment의 dislike_count가 감소합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "스터디 게시글의 id를 입력합니다.", required = true)
    @Parameter(name = "commentId", description = "싫어요를 취소할 댓글의 id를 입력합니다.", required = true)
    @DeleteMapping("/studies/{studyId}/posts/{postId}/comments/{commentId}/dislikes")
    public ApiResponse<StudyPostCommentResponseDTO.CommentPreviewDTO> cancelCommentDislike(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId,
            @PathVariable @ExistStudyPostComment Long commentId) {
        StudyPostCommentResponseDTO.CommentPreviewDTO commentPreviewDTO = studyPostCommandService.cancelCommentDislike(studyId, postId, commentId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_DISLIKE_CANCELED, commentPreviewDTO);
    }

    @Tag(name = "스터디 게시글 - 댓글")
    @Operation(summary = "[스터디 게시글 - 댓글] 전체 댓글 불러오기", description = """ 
        ## [스터디 게시글] 내 스터디 > 스터디 > 게시판 > 게시글 클릭, 로그인한 회원이 참여하는 특정 스터디의 게시글에 달린 모든 댓글을 불러옵니다.
        특정 study_post에 대한 comment(댓/답글) 목록이 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "댓글을 불러올 스터디 게시글의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/posts/{postId}/comments")
    public ApiResponse<StudyPostCommentResponseDTO.CommentReplyListDTO> getAllComments(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId) {
        StudyPostCommentResponseDTO.CommentReplyListDTO commentReplyListDTO = studyPostQueryService.getAllComments(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_COMMENT_FOUND, commentReplyListDTO);
    }

}

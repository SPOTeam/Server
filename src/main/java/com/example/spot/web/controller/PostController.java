package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.post.PostCommandService;
import com.example.spot.service.post.PostQueryService;
import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.validation.annotation.ExistPost;
import com.example.spot.web.dto.post.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/spot/posts")
@Validated
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;


    private final static int PAGE_SIZE = 10; //페이지당 개수

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(
            summary = "[게시판] 게시글 등록 API",
            description = """
        입력 받은 값으로 게시글을 하나 등록 합니다. 
        
        게시글 종류는 PASS_EXPERIENCE, INFORMATION_SHARING, COUNSELING, JOB_TALK, FREE_TALK, SPOT_ANNOUNCEMENT 중 하나입니다.
        
        익명 여부를 선택할 수 있습니다.
        
        생성된 게시글의 고유 ID와 게시글 종류, 생성 시간을 반환합니다.
        """,
            security = @SecurityRequirement(name = "accessToken")
    )
    @PostMapping(value = "/{memberId}")
    public ApiResponse<PostCreateResponse> create(
            @PathVariable @ExistMember Long memberId,
            @RequestBody @Valid PostCreateRequest postCreateRequest
    ) {
        PostCreateResponse response = postCommandService.createPost(memberId, postCreateRequest);
        return ApiResponse.onSuccess(SuccessStatus._CREATED, response);
    }

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(
            summary = "[게시판] 게시글 단건 조회 API",
        description = """
        게시글 ID를 받아 게시글을 조회합니다. 
        
        해당 게시글에 대한 상세 정보를 반환합니다. 
        """,
            security = @SecurityRequirement(name = "accessToken")
    )
    @GetMapping("/{postId}")
    public ApiResponse<PostSingleResponse> singlePost(
            @Parameter(description = "조회할 게시글 ID입니다.", schema = @Schema(type = "integer", format = "int64"))
            @PathVariable @ExistPost Long postId
    ) {
        PostSingleResponse response = postQueryService.getPostById(postId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(
            summary = "[게시판] 게시글 페이지 조회 API",
        description = """
        게시글 종류를 받아 페이지 번호에 해당하는 게시글을 조회합니다.
        
        게시글 종류는 PASS_EXPERIENCE, INFORMATION_SHARING, COUNSELING, JOB_TALK, FREE_TALK, SPOT_ANNOUNCEMENT 중 하나입니다.
        
        페이지 번호는 0부터 시작하며 기본값은 0입니다.
        """
    )
    @GetMapping
    public ApiResponse<PostPagingResponse> getPagingPost(
            @Parameter(description = "게시글 종류. ALL, PASS_EXPERIENCE, INFORMATION_SHARING, COUNSELING, JOB_TALK, FREE_TALK, SPOT_ANNOUNCEMENT 중 하나입니다.", required = true, example = "JOB_TALK")
            @RequestParam String type,

            @Parameter(description = "페이지 번호 (0부터 시작, 기본값 0)", example = "0")
            @RequestParam(required = false, defaultValue = "0") int pageNumber
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);
        PostPagingResponse response = postQueryService.getPagingPosts(type, pageable);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(
            summary = "[게시판] Best 인기글 조회",
            description = "Best 인기글을 조회합니다.(인기글 조회시 종류 명시가 필요합니다.)")
    @GetMapping("/best")
    public ApiResponse<PostBest5Response> getPostBest(
            @Parameter(description = "인기글 종류. REAL_TIME, RECOMMEND, COMMENT 중 하나입니다. 요청하지 않으면 기본 값인 REAL_TIME로 조회됩니다.", example = "REAL_TIME")
            @RequestParam(required = false, defaultValue = "REAL_TIME") String sortType
    ) {
        PostBest5Response response = postQueryService.getPostBest(sortType);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(
            summary = "[게시판] 게시판 홈 게시글 조회",
            description = "게시판 홈에 게시글 종류별로 대표1개씩 게시글을 조회합니다.")
    @GetMapping("/representative")
    public ApiResponse<PostRepresentativeResponse> getPostRepresentative() {
        PostRepresentativeResponse response = postQueryService.getRepresentativePosts();
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(
            summary = "[게시판] 게시판 공지 조회",
            description = "공지를 조회합니다.")
    @GetMapping("/announcement")
    public ApiResponse<PostAnnouncementResponse> getPostAnnouncement() {
        PostAnnouncementResponse response = postQueryService.getPostAnnouncements();
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(
            summary = "[게시판] 게시글 수정 API",
            description = "게시글 Id를 받아 게시글을 수정합니다.",
            security = @SecurityRequirement(name = "accessToken")
    )
    @PatchMapping("/{memberId}/{postId}")
    public ApiResponse<PostCreateResponse> update(
            @PathVariable @ExistMember Long memberId,
            @Parameter(
                    description = "수정할 게시글의 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable @ExistPost Long postId,
            @Parameter(
                    description = "수정할 게시글 데이터입니다."
            )
            @RequestBody PostUpdateRequest postUpdateRequest
    ) {
        PostCreateResponse response = postCommandService.updatePost(memberId, postId, postUpdateRequest);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판", description = "게시판 관련 API")
    @Operation(summary = "[게시판] 게시글 삭제 API", description = "게시글 Id를 받아 게시글을 삭제합니다.")
    @DeleteMapping("/{memberId}/{postId}")
    public ApiResponse<Void> delete(
            @PathVariable @ExistMember Long memberId,
            @Parameter(
                    description = "삭제할 게시글의 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long postId
    ) {
        postCommandService.deletePost(memberId, postId);
        return ApiResponse.onSuccess(SuccessStatus._NO_CONTENT);

    }

    @Tag(name = "게시글 좋아요", description = "게시글 좋아요 관련 API")
    //게시글 좋아요
    @Operation(summary = "[게시판] 게시글 좋아요 API", description = "게시글 Id를 받아 게시글에 좋아요를 추가합니다.")
    @PostMapping("/{postId}/{memberId}/like")
    public ApiResponse<PostLikeResponse> likePost(
            @PathVariable @ExistPost Long postId,
            @PathVariable @ExistMember Long memberId) {
        PostLikeResponse response = postCommandService.likePost(postId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시글 좋아요", description = "게시글 좋아요 관련 API")
    @Operation(summary = "[게시판] 게시글 좋아요 취소 API", description = "게시글 Id를 받아 게시글에 좋아요를 취소합니다.")
    @DeleteMapping("/{postId}/{memberId}/like")
    public ApiResponse<PostLikeResponse> cancelPostLike(
            @PathVariable @ExistPost Long postId,
            @PathVariable @ExistMember Long memberId) {
        PostLikeResponse response = postCommandService.cancelPostLike(postId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._NO_CONTENT, response);
    }


    //댓글
    @Tag(name = "게시판 - 댓글", description = "댓글 관련 API")
    @Operation(summary = "[게시판] 댓글 생성 API",
            description = """
            게시글 Id와 회원 Id를 받아 댓글을 생성합니다.
            
            댓글일 경우 parentCommentId는 0이고, 대댓글일 경우 부모댓글 parentCommentId를 받습니다.
            
            익명 여부 선택할 수 있습니다.
            
            생성된 댓글의 고유 ID와 부모댓글 ID(parentCommentId가 0일 경우 null로 반환), 댓글 내용, 작성자를 반환합니다.
            """)
    @PostMapping("/{postId}/{memberId}/comments")
    public ApiResponse<CommentCreateResponse> createComment(
            @PathVariable @ExistPost Long postId,
            @PathVariable @ExistMember Long memberId,
            @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = postCommandService.createComment(postId, memberId, request);
        return ApiResponse.onSuccess(SuccessStatus._CREATED, response);
    }

    @Tag(name = "게시판 - 댓글", description = "댓글 관련 API")
    @Operation(summary = "!테스트용! 게시글 댓글 조회 API", description = "게시글 ID를 받아 댓글을 조회합니다. 댓글 조회는 이미 게시글 단건 조회에 포함되어 있습니다.")
    @GetMapping("/{postId}/comments")
    public ApiResponse<CommentResponse> getComment(
            @Parameter(
                    description = "조회할 게시글의 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable @ExistPost Long postId
    ) {
        CommentResponse response = postQueryService.getCommentsByPostId(postId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }


    //게시글 댓글 좋아요
    @Tag(name = "게시판 - 댓글", description = "댓글 관련 API")
    @Operation(summary = "[게시판] 댓글 좋아요 API", description = "댓글 ID와 회원 ID를 받아 댓글에 좋아요를 추가합니다.")
    @PostMapping("/comments/{commentId}/{memberId}/like")
    public ApiResponse<CommentLikeResponse> likeComment(
            @PathVariable Long commentId,
            @PathVariable @ExistMember Long memberId) {
        CommentLikeResponse response = postCommandService.likeComment(commentId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판 - 댓글", description = "댓글 관련 API")
    @Operation(summary = "[게시판] 댓글 좋아요 취소 API", description = "댓글 ID와 회원 ID를 받아 댓글에 좋아요를 취소합니다.")
    @DeleteMapping("/comments/{commentId}/{memberId}/like")
    public ApiResponse<CommentLikeResponse> cancelCommentLike(
            @PathVariable Long commentId,
            @PathVariable @ExistMember Long memberId) {
        CommentLikeResponse response = postCommandService.cancelCommentLike(commentId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._NO_CONTENT, response);
    }

    //게시글 댓글 싫어요
    @Tag(name = "게시판 - 댓글", description = "댓글 관련 API")
    @Operation(summary = "[게시판] 댓글 싫어요 API", description = "댓글 ID와 회원 ID를 받아 댓글에 싫어요를 추가합니다.")
    @PostMapping("/comments/{commentId}/{memberId}/dislike")
    public ApiResponse<CommentLikeResponse> dislikeComment(
            @PathVariable Long commentId,
            @PathVariable @ExistMember Long memberId) {
        CommentLikeResponse response = postCommandService.dislikeComment(commentId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Tag(name = "게시판 - 댓글", description = "댓글 관련 API")
    @Operation(summary = "[게시판] 댓글 싫어요 취소 API", description = "댓글 ID와 회원 ID를 받아 댓글에 싫어요를 취소합니다.")
    @DeleteMapping("/comments/{commentId}/{memberId}/dislike")
    public ApiResponse<CommentLikeResponse> cancelCommentDislike(
            @PathVariable Long commentId,
            @PathVariable @ExistMember Long memberId) {
        CommentLikeResponse response = postCommandService.cancelCommentDislike(commentId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._NO_CONTENT, response);
    }

}

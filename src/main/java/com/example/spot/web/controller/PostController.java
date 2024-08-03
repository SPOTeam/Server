package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.post.PostCommandService;
import com.example.spot.service.post.PostQueryService;
import com.example.spot.web.dto.post.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "Post API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot/posts")
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;


    private final static int PAGE_SIZE = 10; //페이지당 개수

    @Operation(
            summary = "게시글 등록 API",
            description = "form/data로 새로운 게시글을 생성합니다.",
            security = @SecurityRequirement(name = "accessToken")
    )
    @PostMapping(value = "/{memberId}")
    public ApiResponse<PostCreateResponse> create(
            @PathVariable Long memberId,
            @RequestBody PostCreateRequest postCreateRequest
    ) {
        PostCreateResponse response = postCommandService.createPost(memberId, postCreateRequest);
        return ApiResponse.onSuccess(SuccessStatus._CREATED, response);
    }

    @Operation(
            summary = "게시글 단건 조회 API",
            description = "게시글 Id를 받아 게시글을 조회합니다.",
            security = @SecurityRequirement(name = "accessToken")
    )
    @GetMapping("/{postId}")
    public ApiResponse<PostSingleResponse> singlePost(
            @Parameter(
                    description = "조회할 게시글 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long postId
    ) {
        PostSingleResponse response = postQueryService.getPostById(postId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Operation(
            summary = "게시글 페이지 조회 API",
            description = "게시글을 게시글 종류를 받아 조회합니다."
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

    @Operation(
            summary = "Best 인기글 조회",
            description = "Best 인기글을 조회합니다.(인기글 조회시 종류 명시가 필요합니다.)")
    @GetMapping("/best")
    public ApiResponse<PostBest5Response> getPostBest(
            @Parameter(description = "인기글 종류. REAL_TIME, RECOMMEND, COMMENT 중 하나입니다. 요청하지 않으면 기본 값인 REAL_TIME로 조회됩니다.", example = "REAL_TIME")
            @RequestParam(required = false, defaultValue = "REAL_TIME") String sortType
    ) {
        PostBest5Response response = postQueryService.getPostBest(sortType);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Operation(
            summary = "게시판 홈 게시글 조회",
            description = "게시판 홈에 게시글 종류별로 대표1개씩 게시글을 조회합니다.")
    @GetMapping("/representative")
    public ApiResponse<PostRepresentativeResponse> getPostRepresentative() {
        PostRepresentativeResponse response = postQueryService.getRepresentativePosts();
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Operation(
            summary = "게시판 공지 조회",
            description = "공지를 조회합니다.")
    @GetMapping("/announcement")
    public ApiResponse<PostAnnouncementResponse> getPostAnnouncement() {
        PostAnnouncementResponse response = postQueryService.getPostAnnouncements();
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Operation(
            summary = "게시글 수정 API",
            description = "게시글 Id를 받아 게시글을 수정합니다.",
            security = @SecurityRequirement(name = "accessToken")
    )
    @PatchMapping("/{memberId}/{postId}")
    public ApiResponse<PostCreateResponse> update(
            @PathVariable Long memberId,
            @Parameter(
                    description = "수정할 게시글의 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long postId,
            @Parameter(
                    description = "수정할 게시글 데이터입니다."
            )
            @RequestBody PostUpdateRequest postUpdateRequest
    ) {
        PostCreateResponse response = postCommandService.updatePost(memberId, postId, postUpdateRequest);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Operation(summary = "게시글 삭제 API", description = "게시글 Id를 받아 게시글을 삭제합니다.")
    @DeleteMapping("/{memberId}/{postId}")
    public ApiResponse<Void> delete(
            @PathVariable Long memberId,
            @Parameter(
                    description = "삭제할 게시글의 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long postId
    ) {
        postCommandService.deletePost(memberId, postId);
        return ApiResponse.onSuccess(SuccessStatus._NO_CONTENT);

    }

    //게시글 좋아요
    @Operation(summary = "게시글 좋아요 API", description = "게시글 Id를 받아 게시글에 좋아요를 추가합니다.")
    @PostMapping("/{postId}/{memberId}/like")
    public ApiResponse<PostLikeResponse> likePost(
            @PathVariable Long postId,
            @PathVariable Long memberId) {
        PostLikeResponse response = postCommandService.likePost(postId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._OK, response);
    }

    @Operation(summary = "게시글 좋아요 취소 API", description = "게시글 Id를 받아 게시글에 좋아요를 취소합니다.")
    @DeleteMapping("/{postId}/{memberId}/like")
    public ApiResponse<PostLikeResponse> cancelPostLike(
            @PathVariable Long postId,
            @PathVariable Long memberId) {
        PostLikeResponse response = postCommandService.cancelPostLike(postId, memberId);
        return ApiResponse.onSuccess(SuccessStatus._NO_CONTENT, response);
    }


    @Operation(summary = "댓글 생성 API", description = "게시글 Id와 회원 Id를 받아 댓글을 생성합니다.")
    @PostMapping("/{postId}/{memberId}/comments")
    public ApiResponse<CommentCreateResponse> createComment(
            @PathVariable Long postId,
            @PathVariable Long memberId,
            @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = postCommandService.createComment(postId, memberId, request);
        return ApiResponse.onSuccess(SuccessStatus._CREATED, response);
    }

    @Operation(summary = "댓글 조회 API", description = "댓글 ID를 받아 댓글을 조회합니다.")
    @GetMapping("/comments/{commentId}")
    public void getComment(
            @Parameter(
                    description = "조회할 댓글의 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long commentId
    ) {
        //메서드
    }

}

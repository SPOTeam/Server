package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.web.dto.post.PostBest5Response;
import com.example.spot.web.dto.post.PostCreateRequest;
import com.example.spot.web.dto.post.PostPagingResponse;
import com.example.spot.web.dto.post.PostSingleResponse;
import com.example.spot.web.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "Post API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot/posts")
public class PostController {

    private final PostService postService;

    private final static int PAGE_SIZE = 10; //페이지당 개수

    @Operation(summary = "게시글 등록 API", description = "form/data로 새로운 게시글을 생성합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void create(@ModelAttribute PostCreateRequest postCreateRequest) {
        postService.createPost(postCreateRequest);
        // ToDo 응답 통일한 후 반환 타입 수정
    }

    @Operation(summary = "게시글 단건 조회 API", description = "게시글 Id를 받아 게시글을 조회합니다.")
    @GetMapping("/{postId}")
    public ApiResponse<PostSingleResponse> singlePost(
            @Parameter(
                    description = "조회할 게시글 ID입니다.",
                    schema = @Schema(type = "intger", format = "int64")
            )
            @PathVariable Long postId
    ) {
        PostSingleResponse postSingleResponse = postService.getSinglePost(postId);
        return ApiResponse.onSuccess(SuccessStatus._OK, postSingleResponse);
    }

    @Operation(summary = "게시글 페이지 조회 API", description = "게시글을 게시글 종류를 받아 조회합니다.")
    @GetMapping
    public ApiResponse<PostPagingResponse> getPagingPost(
            @Parameter(description = "게시글 종류. ALL, PASS_EXPERIENCE, INFORMATION_SHARING, COUNSELING, JOB_TALK, FREE_TALK, SPOT_ANNOUNCEMENT 중 하나입니다.", required = true, example = "JOB_TALK")
            @RequestParam String type,

            @Parameter(description = "페이지 번호 (0부터 시작, 기본값 0)", example = "0")
            @RequestParam(required = false, defaultValue = "0") int pageNumber
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);
        PostPagingResponse postPagingResponse = postService.postTypePaging(type, pageable);
        return ApiResponse.onSuccess(SuccessStatus._OK, postPagingResponse);
    }

    @Operation(
            summary = "게시판 홈 조회",
            description = "인기글(인기글 조회시 종류 명시가 필요합니다.) & 게시글 5개(종류별 1개씩) & 공지 5개로 홈화면을 조회합니다.")
    @GetMapping("/home")
    public ApiResponse<PostHomeResponse> getPostHome(
            @Parameter(description = "인기글 종류. REAL_TIME, RECOMMEND, COMMENT 중 하나입니다.", required = true, example = "REAL_TIME")
            @RequestParam String sortType
    ) {
        PostHomeResponse postHomeResponse = postService.getPostHome(sortType);
        return ApiResponse.onSuccess(SuccessStatus._OK, postHomeResponse);
    }


    @Operation(summary = "게시글 삭제 API", description = "게시글 Id를 받아 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public void delete(
            @Parameter(
                    description = "삭제할 게시글의 ID입니다.",
                    schema = @Schema(type = "integer", format = "int64")
            )
            @PathVariable Long postId
    ) {
        postService.deletePost(postId);
    }



}

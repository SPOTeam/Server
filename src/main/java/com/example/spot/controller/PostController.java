package com.example.spot.controller;

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
@RequestMapping("/spot/api/posts")
public class PostController {

    private final PostService postService;

    private final static int PAGE_SIZE = 10; //페이지당 개수

    @Operation(summary = "게시글 등록 API", description = "form/data로 새로운 게시글을 생성합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void create(@ModelAttribute PostCreateRequest postCreateRequest) {
        postService.createPost(postCreateRequest);
        // ToDo 응답 통일한 후 반환 타입 수정
    }

    @Operation(summary = "게시글 조회 API", description = "게시글 Id를 받아 게시글을 조회합니다.")
    @GetMapping("/{postId}")
    public void singlePost(
            @Parameter(
                    description = "조회할 게시글 ID입니다.",
                    schema = @Schema(type = "intger", format = "int64")
            )
            @PathVariable Long postId
    ) {
        postService.getSinglePost(postId);
    }

    @Operation(summary = "게시글 페이지 조회 API", description = "게시글을 게시글 종류를 받아 조회합니다.")
    @GetMapping
    public void getPagingPost(
            @Parameter(description = "게시글 종류. ALL, PASS_EXPERIENCE, INFORMATION_SHARING, COUNSELING, JOB_TALK, FREE_TALK, SPOT_ANNOUNCEMENT 중 하나입니다.", required = true, example = "JOB_TALK")
            @RequestParam String type,

            @Parameter(description = "페이지 번호 (0부터 시작, 기본값 0)", example = "0")
            @RequestParam(required = false, defaultValue = "0") int pageNumber
    ) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);
        postService.postTypePaging(type, pageable);
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

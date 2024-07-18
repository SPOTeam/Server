package com.example.spot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "Post API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot/api/posts")
public class PostController {

    private final PostService postService;

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

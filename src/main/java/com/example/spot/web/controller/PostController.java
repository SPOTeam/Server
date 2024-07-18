package com.example.spot.web.controller;

import com.example.spot.web.dto.PostCreateRequest;
import com.example.spot.web.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
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

}

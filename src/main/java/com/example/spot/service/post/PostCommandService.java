package com.example.spot.service.post;

import com.example.spot.web.dto.post.PostCreateResponse;
import com.example.spot.web.dto.post.PostCreateRequest;
import com.example.spot.web.dto.post.PostUpdateRequest;
import org.springframework.transaction.annotation.Transactional;


public interface PostCommandService {

    PostCreateResponse createPost(PostCreateRequest postCreateRequest);

    @Transactional
    void deletePost(Long postId);

    @Transactional
    PostCreateResponse updatePost(Long postId, PostUpdateRequest postUpdateRequest);

}

package com.example.spot.service.post;

import com.example.spot.web.dto.post.PostCreateResponse;
import com.example.spot.web.dto.post.PostCreateRequest;
import com.example.spot.web.dto.post.PostUpdateRequest;
import org.springframework.transaction.annotation.Transactional;


public interface PostCommandService {

    PostCreateResponse createPost(Long memberId, PostCreateRequest postCreateRequest);

    void deletePost(Long memberId, Long postId);

    PostCreateResponse updatePost(Long memberId, Long postId, PostUpdateRequest postUpdateRequest);

}

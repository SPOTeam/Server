package com.example.spot.service.post;

import com.example.spot.web.dto.post.*;
import org.springframework.transaction.annotation.Transactional;


public interface PostCommandService {

    PostCreateResponse createPost(Long memberId, PostCreateRequest postCreateRequest);

    void deletePost(Long memberId, Long postId);

    PostCreateResponse updatePost(Long memberId, Long postId, PostUpdateRequest postUpdateRequest);

    PostLikeResponse likePost(Long postId, Long memberId);

    PostLikeResponse cancelPostLike(Long postId, Long memberId);

}

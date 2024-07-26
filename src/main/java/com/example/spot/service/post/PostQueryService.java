package com.example.spot.service.post;

import com.example.spot.web.dto.post.PostSingleResponse;

public interface PostQueryService {
    PostSingleResponse getPostById(Long postId);

}

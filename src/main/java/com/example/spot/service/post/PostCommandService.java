package com.example.spot.service.post;

import com.example.spot.web.dto.post.PostCreateResponse;
import com.example.spot.web.dto.post.PostCreateRequest;


public interface PostCommandService {

    PostCreateResponse createPost(PostCreateRequest postCreateRequest);

}

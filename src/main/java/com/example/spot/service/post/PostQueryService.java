package com.example.spot.service.post;

import com.example.spot.web.dto.post.PostBest5Response;
import com.example.spot.web.dto.post.PostPagingResponse;
import com.example.spot.web.dto.post.PostRepresentativeResponse;
import com.example.spot.web.dto.post.PostSingleResponse;
import org.springframework.data.domain.Pageable;

public interface PostQueryService {
    PostSingleResponse getPostById(Long postId);

    PostPagingResponse getPagingPosts(String type, Pageable pageable);

    PostBest5Response getPostBest(String sortType);

    PostRepresentativeResponse getRepresentativePosts();

}

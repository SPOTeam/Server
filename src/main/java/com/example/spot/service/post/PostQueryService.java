package com.example.spot.service.post;

import com.example.spot.web.dto.post.*;
import org.springframework.data.domain.Pageable;

public interface PostQueryService {
    PostSingleResponse getPostById(Long postId);

    PostPagingResponse getPagingPosts(String type, Pageable pageable);

    PostBest5Response getPostBest(String sortType);

    PostRepresentativeResponse getRepresentativePosts();

    PostAnnouncementResponse getPostAnnouncements();

}

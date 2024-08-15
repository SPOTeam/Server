package com.example.spot.service.post;

public interface LikedPostQueryService {
    long countByPostId(Long postId);

    boolean existsByMemberIdAndPostId(Long postId);
}

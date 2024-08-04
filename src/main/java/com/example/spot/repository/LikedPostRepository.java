package com.example.spot.repository;

import com.example.spot.domain.LikedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikedPostRepository extends JpaRepository<LikedPost, Long> {
    Optional<LikedPost> findByMemberIdAndPostId(Long memberId, Long postId);

    // 게시글 ID별로 LikedPost의 개수 세기
    long countByPostId(Long postId);

}

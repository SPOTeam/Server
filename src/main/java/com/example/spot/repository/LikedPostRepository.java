package com.example.spot.repository;

import com.example.spot.domain.LikedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikedPostRepository extends JpaRepository<LikedPost, Long> {
    Optional<LikedPost> findByMemberIdAndPostId(Long memberId, Long postId);

}

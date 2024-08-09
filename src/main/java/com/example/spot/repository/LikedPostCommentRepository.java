package com.example.spot.repository;

import com.example.spot.domain.LikedPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikedPostCommentRepository extends JpaRepository<LikedPostComment, Long> {
    Optional<LikedPostComment> findByMemberIdAndPostCommentIdAndIsLikedTrue(Long memberId, Long postCommentId);
    Optional<LikedPostComment> findByMemberIdAndPostCommentIdAndIsLikedFalse(Long memberId, Long postCommentId);
    long countByPostCommentIdAndIsLikedTrue(Long postCommentId);
}

package com.example.spot.repository;

import com.example.spot.domain.LikedPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikedPostCommentRepository extends JpaRepository<LikedPostComment, Long> {
    Optional<LikedPostComment> findByMemberIdAndPostCommentIdAndIsLikedTrue(Long memberId, Long postCommentId);
    Optional<LikedPostComment> findByMemberIdAndPostCommentIdAndIsLikedFalse(Long memberId, Long postCommentId);
    long countByPostCommentIdAndIsLikedTrue(Long postCommentId);
    long countByPostCommentIdAndIsLikedFalse(Long postCommentId);
    //회원 ID와 댓글 ID로 좋아요(IsLiked=True) 존재 여부
    boolean existsByMemberIdAndPostCommentIdAndIsLikedTrue(Long currentUserId, Long postCommentId);
    //회원 ID와 댓글 ID로 싫어요(IsLiked=False) 존재 여부
    boolean existsByMemberIdAndPostCommentIdAndIsLikedFalse(Long currentUserId, Long postCommentId);
}

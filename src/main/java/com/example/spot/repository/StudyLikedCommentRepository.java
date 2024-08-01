package com.example.spot.repository;

import com.example.spot.domain.mapping.StudyLikedComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyLikedCommentRepository extends JpaRepository<StudyLikedComment, Long> {

    Optional<StudyLikedComment> findByMemberIdAndStudyPostCommentId(Long memberId, Long commentId);
    Optional<StudyLikedComment> findByMemberIdAndStudyPostCommentIdAndIsLiked(Long memberId, Long commentId, Boolean isLiked);
}

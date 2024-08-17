package com.example.spot.repository;

import com.example.spot.domain.mapping.StudyLikedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyLikedPostRepository extends JpaRepository<StudyLikedPost, Long> {

    Optional<StudyLikedPost> findByMemberIdAndStudyPostId(Long memberId, Long postId);

    boolean existsByMemberIdAndStudyPostId(Long memberId, Long id);
}

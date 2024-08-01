package com.example.spot.repository;

import com.example.spot.domain.study.StudyPostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyPostCommentRepository extends JpaRepository<StudyPostComment, Long> {

    List<StudyPostComment> findByMemberIdAndStudyPostId(Long id, Long postId);

    List<StudyPostComment> findByStudyPostId(Long postId);
}

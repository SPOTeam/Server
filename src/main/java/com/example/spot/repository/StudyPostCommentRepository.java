package com.example.spot.repository;

import com.example.spot.domain.study.StudyPostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostCommentRepository extends JpaRepository<StudyPostComment, Long> {
}

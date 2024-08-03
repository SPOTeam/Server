package com.example.spot.repository;

import com.example.spot.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByIdAndStudyId(Long id, Long studyId);

    @Query("SELECT q FROM Quiz q WHERE q.study.id = :studyId AND q.createdAt >= :startOfDay AND q.createdAt <= :endOfDay")
    List<Quiz> findAllByStudyIdAndCreatedAtBetween(@Param("studyId") Long studyId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

}

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

    List<Quiz> findByScheduleId(Long scheduleId);

    List<Quiz> findAllByScheduleIdAndCreatedAtBetween(Long scheduleId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}

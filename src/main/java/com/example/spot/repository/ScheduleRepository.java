package com.example.spot.repository;

import com.example.spot.domain.study.Schedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByStudyId(Long studyId, Pageable pageable);

    List<Schedule> findByStudyId(Long studyId);

    Optional<Schedule> findByIdAndStudyId(Long id, Long studyId);

    Optional<Schedule> findByIdAndMemberId(Long scheduleId, Long memberId);
}

package com.example.spot.repository;

import com.example.spot.domain.study.ToDoList;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {
    List<ToDoList> findByStudyId(Long studyId, Pageable pageable);
    Long countByStudyIdAndMemberIdAndDate(Long studyId, Long memberId, LocalDate date);
    List<ToDoList> findByStudyIdAndMemberIdAndDateOrderByCreatedAtDesc(Long studyId, Long memberId, LocalDate date, Pageable pageable);

}

package com.example.spot.repository;

import com.example.spot.domain.study.ToDoList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToDoListRepository extends JpaRepository<ToDoList, Long> {
    List<ToDoList> findByStudyId(Long studyId, Pageable pageable);
    Long countByStudyIdAndMemberId(Long studyId, Long memberId);
    List<ToDoList> findByStudyIdAndMemberIdOrderByCreatedAtDesc(Long studyId, Long memberId, Pageable pageable);

}

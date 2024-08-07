package com.example.spot.repository;

import com.example.spot.domain.StudyReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyReasonRepository extends JpaRepository<StudyReason, Long> {

    boolean existsByMemberId(Long memberId);
    void deleteByMemberId(Long memberId);

}

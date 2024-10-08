package com.example.spot.repository;

import com.example.spot.domain.study.StudyPostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostReportRepository extends JpaRepository<StudyPostReport, Long> {

    void deleteAllByStudyPostId(Long postId);
}

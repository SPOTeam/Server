package com.example.spot.repository;

import com.example.spot.domain.study.StudyPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {
    Optional<StudyPost> findByStudyIdAndAnnouncementIs(Long studyId, boolean isAnnouncement);
}

package com.example.spot.repository;

import com.example.spot.domain.mapping.StudyPostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostImageRepository extends JpaRepository<StudyPostImage, Long> {
    void deleteAllByStudyPostId(Long studyPostId);
}

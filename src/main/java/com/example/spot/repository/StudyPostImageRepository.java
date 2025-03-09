package com.example.spot.repository;

import com.example.spot.domain.mapping.StudyPostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StudyPostImageRepository extends JpaRepository<StudyPostImage, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM StudyPostImage spi WHERE spi.studyPost.id = :studyPostId")
    void deleteAllByStudyPostId(@Param("studyPostId") Long studyPostId);
}

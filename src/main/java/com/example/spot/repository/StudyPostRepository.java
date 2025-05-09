package com.example.spot.repository;

import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.study.StudyPost;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.example.spot.repository.querydsl.StudyPostRepositoryCustom;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostRepository extends JpaRepository<StudyPost, Long>, StudyPostRepositoryCustom {

    Optional<StudyPost> findByStudyIdAndIsAnnouncement(Long studyId, boolean isAnnouncement);

    Optional<StudyPost> findByIdAndStudyId(Long postId, Long studyId);

    Optional<StudyPost> findByIdAndMemberId(Long postId, Long memberId);

    Long countByStudyId(Long studyId);

    Long countByStudyIdAndIsAnnouncement(Long studyId, Boolean aTrue);

    Long countByStudyIdAndTheme(Long studyId, Theme theme);
}

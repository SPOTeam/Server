package com.example.spot.repository;

import com.example.spot.domain.enums.StudyLikeStatus;
import com.example.spot.domain.mapping.PreferredStudy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 찜한 스터디
public interface PreferredStudyRepository extends JpaRepository<PreferredStudy, Long> {

    List<PreferredStudy> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    Optional<PreferredStudy> findByMemberIdAndStudyId(Long memberId, Long studyId);

    long countByMemberId(Long memberId);
}

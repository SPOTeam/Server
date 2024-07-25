package com.example.spot.repository;

import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.mapping.MemberStudy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberStudyRepository extends JpaRepository<MemberStudy, Long> {

    List<MemberStudy> findByMemberId(Long memberId);

    List<MemberStudy> findAllByMemberIdAndStatus(Long memberId, ApplicationStatus status);

    List<MemberStudy> findAllByMemberIdAndIsOwned(Long memberId, Boolean isOwned);

    List<MemberStudy> findAllByStudyIdAndStatus(Long studyId, ApplicationStatus status);

    Optional<MemberStudy> findByMemberIdAndStudyId(Long memberId, Long studyId);
}

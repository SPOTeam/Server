package com.example.spot.repository;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.mapping.MemberStudy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberStudyRepository extends JpaRepository<MemberStudy, Long> {

    List<MemberStudy> findByMemberIdAndStatusNot(Long memberId, ApplicationStatus status);

    List<MemberStudy> findAllByMemberIdAndStatus(Long memberId, ApplicationStatus status);

    List<MemberStudy> findAllByMemberIdAndIsOwned(Long memberId, Boolean isOwned);

    List<MemberStudy> findAllByStudyIdAndStatus(Long studyId, ApplicationStatus status);

    Optional<MemberStudy> findByMemberIdAndStudyIdAndStatus(Long memberId, Long studyId, ApplicationStatus status);

    Optional<MemberStudy> findByMemberIdAndStudyIdAndIsOwned(Long memberId, Long studyId, Boolean isOwned);

    Optional<MemberStudy> findByMemberIdAndStudyId(Long memberId, Long studyId);

    long countByStatusAndStudyId(ApplicationStatus status, Long studyId);
    long countByMemberIdAndStatusAndStudy_Status(Long memberId, ApplicationStatus applicationStatus, Status status);
    long countByMemberIdAndIsOwnedAndStudy_Status(Long memberId, Boolean isOwned, Status status);

    boolean existsByMemberIdAndStudyIdAndStatus(Long memberId, Long studyId, ApplicationStatus applicationStatus);

    Optional<MemberStudy> findByStudyIdAndIsOwned(Long studyId, boolean b);

    boolean existsByMemberIdAndIsOwned(Long memberId, boolean b);
}

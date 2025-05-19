package com.example.spot.repository;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.mapping.MemberStudy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(
            value = """
        SELECT ms.*
        FROM member_study ms
        JOIN study s ON ms.study_id = s.id
        WHERE ms.member_id = :memberId
          AND ms.status = 'APPROVED'
          AND (
                s.study_state = 'COMPLETED'
                OR ms.active_status = 'OFF'
              )
        ORDER BY ms.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM member_study ms
        JOIN study s ON ms.study_id = s.id
        WHERE ms.member_id = :memberId
          AND ms.status = 'APPROVED'
          AND (
                s.study_state = 'COMPLETED'
                OR ms.active_status = 'OFF'
              )
        """,
            nativeQuery = true
    )
    Page<MemberStudy> findAllByMemberIdAndConditions(@Param("memberId") Long memberId, Pageable pageable);


    long countByStatusAndStudyId(ApplicationStatus status, Long studyId);
    long countByMemberIdAndStatusAndStudy_Status(Long memberId, ApplicationStatus applicationStatus, Status status);
    long countByMemberIdAndIsOwnedAndStudy_Status(Long memberId, Boolean isOwned, Status status);

    boolean existsByMemberIdAndStudyIdAndStatus(Long memberId, Long studyId, ApplicationStatus applicationStatus);

    Optional<MemberStudy> findByStudyIdAndIsOwned(Long studyId, boolean b);

    boolean existsByMemberIdAndIsOwned(Long memberId, boolean b);
}

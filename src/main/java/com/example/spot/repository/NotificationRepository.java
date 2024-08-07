package com.example.spot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spot.domain.Notification;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<Notification> findAllByMemberIdAndStudyIdIn(Long memberId, List<Long> studyIds, Pageable pageable);
    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);
    boolean existsByMemberIdAndIsCheckedFalse(Long memberId);
    boolean existsByMemberIdAndIsReadFalse(Long memberId);
    Optional<Notification> findByMemberIdAndStudyId(Long memberId, Long studyId);

}

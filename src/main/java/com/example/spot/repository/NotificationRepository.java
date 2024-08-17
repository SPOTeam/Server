package com.example.spot.repository;

import com.example.spot.domain.enums.NotifyType;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spot.domain.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByMemberIdAndStudyIdAndType(Long memberId, Long studyId, NotifyType type);
    List<Notification> findByMemberIdAndTypeNot(Long memberId, Pageable pageable, NotifyType type);
    List<Notification> findByMemberIdAndType(Long memberId, Pageable pageable, NotifyType type);

    List<Notification> findByType(NotifyType type);
}

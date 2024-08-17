package com.example.spot.repository;

import com.example.spot.domain.enums.NotifyType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spot.domain.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberId(Long memberId, Pageable pageable);
    List<Notification> findByType(NotifyType type);
}

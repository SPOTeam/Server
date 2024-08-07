package com.example.spot.service.notification;

import org.springframework.data.domain.Pageable;

import com.example.spot.web.dto.notification.NotificationResponseDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationDTO;

public interface NotificationQueryService {
    NotificationResponseDTO.NotificationListDTO getAllNotifications(Long memberId, Pageable pageable);
    NotificationResponseDTO.NotificationListDTO getAllAppliedStudyNotification(Long memberId, Pageable pageable);
    NotificationDTO getAppliedStudyNotification(Long memberId, Long studyId);
    NotificationResponseDTO.NotificationDTO getNotification(Long memberId, Long notificationId);
    boolean existsUnreadNotification(Long memberId);
}
package com.example.spot.service.notification;

import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StudyNotificationListDTO;

import org.springframework.data.domain.Pageable;

public interface NotificationQueryService {

    // 생성된 알림 전체 조회
    NotificationListDTO getAllNotifications(Long memberId, Pageable pageable);

    // 신청한 스터디 알림 전체 조회
    StudyNotificationListDTO getAllAppliedStudyNotification(Long memberId, Pageable pageable);
}

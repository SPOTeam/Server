package com.example.spot.service.notification;

import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StduyNotificationListDTO;
import java.util.List;

import com.example.spot.web.dto.notification.NotificationResponseDTO;

public interface NotificationQueryService {

    // 생성된 알림 전체 조회
    NotificationListDTO getAllNotifications(Long memberId);

    // 신청한 스터디 알림 전체 조회
    StduyNotificationListDTO getAllAppliedStudyNotification(Long memberId);
}

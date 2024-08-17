package com.example.spot.service.notification;

import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StduyNotificationListDTO;
import java.util.List;

import com.example.spot.web.dto.notification.NotificationResponseDTO;

public interface NotificationQueryService {

    NotificationListDTO getAllNotifications(Long memberId);
    StduyNotificationListDTO getAllAppliedStudyNotification(Long memberId);
}

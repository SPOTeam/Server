package com.example.spot.web.service.notification;

import java.util.List;

import com.example.spot.web.dto.notification.NotificationResponseDTO;

public interface NotificationQueryService {

    List<NotificationResponseDTO.NotificationDTO> getAllNotifications(Long memberId);
    List<NotificationResponseDTO.NotificationDTO> getAllAppliedStudyNotification(Long memberId);
    Boolean exitsNotification(long memberId);
}

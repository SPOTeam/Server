package com.example.spot.service.notification;

import java.util.List;

import com.example.spot.web.dto.notification.NotificationResponseDTO;

public interface NotificationQueryService {

    // 모든 알림 조회
    List<NotificationResponseDTO.NotificationDTO> getAllNotifications(Long memberId);

    // 신청 스터디 알림 조회
    List<NotificationResponseDTO.NotificationDTO> getAllAppliedStudyNotification(Long memberId);

    // 알림 존재 여부 확인
    Boolean exitsNotification(Long memberId);
}
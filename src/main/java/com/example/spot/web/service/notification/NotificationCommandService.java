package com.example.spot.web.service.notification;

import com.example.spot.web.dto.notification.NotificationRequestDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO;

public interface NotificationCommandService {

    // 알림 읽음 처리
    NotificationResponseDTO.NotificationDTO readNotification(Long memberId, Long notificationId);

    // 신청한 스터디 참가
    NotificationResponseDTO.NotificationDTO joinAppliedStudy(long studyId, Long userId, NotificationRequestDTO.joinStudyDTO joinStudyDTO);

    // 신청한 스터디 거절
    NotificationResponseDTO.NotificationDTO rejectAppliedStudy(long studyId, Long userId, NotificationRequestDTO.rejectStudyDTO rejectStudyDTO);


}

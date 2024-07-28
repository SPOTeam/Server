package com.example.spot.service.notification;

import com.example.spot.web.dto.notification.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO;

@Service
@RequiredArgsConstructor
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;


    @Override
    public NotificationResponseDTO.NotificationDTO readNotification(Long memberId, Long notificationId) {
        return null;
    }

    @Override
    public NotificationResponseDTO.NotificationDTO joinAppliedStudy(long studyId, Long userId, NotificationRequestDTO.joinStudyDTO joinStudyDTO) {
        return null;
    }

    @Override
    public NotificationResponseDTO.NotificationDTO rejectAppliedStudy(long studyId, Long userId, NotificationRequestDTO.rejectStudyDTO rejectStudyDTO) {
        return null;
    }
}

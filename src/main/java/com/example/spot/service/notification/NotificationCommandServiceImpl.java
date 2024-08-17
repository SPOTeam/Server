package com.example.spot.service.notification;

import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationProcessDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO;

@Service
@RequiredArgsConstructor
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;


    @Override
    public NotificationProcessDTO readNotification(Long memberId, Long notificationId) {
        return null;
    }

    @Override
    public NotificationProcessDTO joinAppliedStudy(Long studyId, Long memberId, boolean isAccept) {
        return null;
    }

}

package com.example.spot.service.notification;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO;

@Service
@RequiredArgsConstructor
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponseDTO.NotificationDTO> getAllAppliedStudyNotification(Long memberId) {
        return List.of();
    }

    @Override
    public Boolean exitsNotification(long memberId) {
        return null;
    }

    @Override
    public List<NotificationResponseDTO.NotificationDTO> getAllNotifications(Long memberId) {
        return null;
    }

}

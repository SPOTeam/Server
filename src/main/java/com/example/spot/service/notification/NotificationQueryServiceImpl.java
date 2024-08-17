package com.example.spot.service.notification;

import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StduyNotificationListDTO;
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
    public StduyNotificationListDTO getAllAppliedStudyNotification(Long memberId) {
        return null;
    }


    @Override
    public NotificationListDTO getAllNotifications(Long memberId) {
        return null;
    }

}

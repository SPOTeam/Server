package com.example.spot.web.dto.notification;

import com.example.spot.domain.Notification;

import com.example.spot.domain.enums.NotifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

@Getter
public class NotificationResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class NotificationDTO {
        private Long id;
        private String title;
        private String content;
        private NotifyType type;
        private boolean isChecked;

        public static NotificationDTO from(Notification notification) {
            return NotificationDTO.builder()
                    .id(notification.getId())
                    .content(notification.getContent())
                    .type(notification.getType())
                    .isChecked(notification.markAsRead())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class NotificationListDTO {
        private List<NotificationDTO> notifications;
        private int totalPages;
        private long totalElements;

        public static NotificationListDTO of(Page<Notification> notifications) {
            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(NotificationDTO::from)
                    .collect(Collectors.toList());
                    
            return NotificationListDTO.builder()
                    .notifications(notificationDTOs)
                    .totalPages(notifications.getTotalPages())
                    .totalElements(notifications.getTotalElements())
                    .build();
        }
    }
}

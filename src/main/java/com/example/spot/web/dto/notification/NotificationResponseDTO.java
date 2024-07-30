package com.example.spot.web.dto.notification;

import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.NotifyType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class NotificationResponseDTO {

    @Getter
    public static class NotificationDTO {

        private final Long notificationId;
        private final String title;
        private final String content;
        private final NotifyType type;
        private final Boolean isChecked;
        private final LocalDateTime createdAt;

        @Builder(access = AccessLevel.PRIVATE)
        private NotificationDTO(Long notificationId, String title, String content, NotifyType type, Boolean isChecked, LocalDateTime createdAt) {
            this.notificationId = notificationId;
            this.title = title;
            this.content = content;
            this.type = type;
            this.isChecked = isChecked;
            this.createdAt = createdAt;
        }

        public static NotificationDTO fromEntity(Notification notification) {
            return NotificationDTO.builder()
                    .notificationId(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .type(notification.getType())
                    .isChecked(notification.getIsChecked())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }
}

package com.example.spot.web.dto.notification;

import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.NotifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDTO {
        public NotificationDTO(Notification notification) {

            this.notificationId = notification.getId();
            this.title = notification.getTitle();
            this.content = notification.getContent();
            this.type = notification.getType();
            this.isChecked = notification.getIsChecked();
            this.createdAt = notification.getCreatedAt();

        }

        Long notificationId;
        String title;
        String content;
        NotifyType type;
        Boolean isChecked;
        LocalDateTime createdAt;
    }
}

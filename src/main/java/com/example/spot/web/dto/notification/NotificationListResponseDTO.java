package com.example.spot.web.dto.notification;

import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.NotifyType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationListResponseDTO {

    private final Long notificationId;
    private final String title;
    private final String content;
    private final NotifyType type;
    private final Boolean isChecked;
    private final LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    public NotificationListResponseDTO(Long notificationId, String title, String content, NotifyType type, Boolean isChecked, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isChecked = isChecked;
        this.createdAt = createdAt;
    }

    public static NotificationListResponseDTO fromEntity(Notification notification) {
        return NotificationListResponseDTO.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isChecked(notification.getIsChecked())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

package com.example.spot.web.dto.notification;

import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.NotifyType;
import java.util.List;
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
    public static class NotificationListDTO {

        List<NotificationDTO> notifications;
        Long totalNotificationCount;
        Long uncheckedNotificationCount;

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NotificationDTO{
            Long notificationId;
            String studyTitle;
            String notifierName;
            NotifyType type;
            Boolean isChecked;
            LocalDateTime createdAt;
        }
    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StduyNotificationListDTO {

        List<StudyNotificationDTO> notifications;
        Long totalNotificationCount;
        Long uncheckedNotificationCount;

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StudyNotificationDTO{
            Long notificationId;
            String studyTitle;
            String studyProfileImage;
            NotifyType type;
            Boolean isChecked;
            LocalDateTime createdAt;
        }
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationProcessDTO {
        boolean isAccept;
        LocalDateTime processedAt;
    }
}

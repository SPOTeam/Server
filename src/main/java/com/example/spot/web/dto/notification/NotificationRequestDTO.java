package com.example.spot.web.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class NotificationRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class readDTO {
        private Long memberId;
        private Long notificationId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class joinStudyDTO {
        private long studyId;
        private Long memberId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class rejectStudyDTO {
        private long studyId;
        private Long memberId;
    }
}

package com.example.spot.web.dto.memberstudy.request;

import com.example.spot.domain.enums.Period;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter

public class ScheduleRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDTO {

        private String title;
        private String location;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
        private Boolean isAllDay; // 종일 진행 여부
        private Period period; // 반복 일정 여부
    }

}

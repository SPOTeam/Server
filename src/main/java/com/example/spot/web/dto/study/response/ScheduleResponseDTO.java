package com.example.spot.web.dto.study.response;

import com.example.spot.domain.study.Schedule;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class ScheduleResponseDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class ScheduleDTO {

        private final Long studyId;
        private final Long scheduleId;
        private final String title;
        private final LocalDateTime startedAt;
        private final LocalDateTime finishedAt;

        public static ScheduleDTO toDTO(Schedule schedule) {
            return ScheduleDTO.builder()
                    .studyId(schedule.getStudy().getId())
                    .scheduleId(schedule.getId())
                    .title(schedule.getTitle())
                    .startedAt(schedule.getStartedAt())
                    .finishedAt(schedule.getFinishedAt())
                    .build();
        }
    }

    //@Getter
    //@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    //@Builder(access = AccessLevel.PRIVATE)
    //public class MonthlyScheduleDTO {
//
    //    private
    //}
}

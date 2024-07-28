package com.example.spot.web.dto.study.response;

import com.example.spot.domain.enums.Period;
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class MonthlyScheduleListDTO {

        private final Long studyId;
        private final List<MonthlyScheduleDTO> scheduleList;

        public static MonthlyScheduleListDTO toDTO(Study study, List<MonthlyScheduleDTO> scheduleList) {
            return MonthlyScheduleListDTO.builder()
                    .studyId(study.getId())
                    .scheduleList(scheduleList)
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class MonthlyScheduleDTO {

        private final Long scheduleId;
        private final String title;
        private final String location;
        private final LocalDateTime startedAt;
        private final LocalDateTime finishedAt;
        private final Boolean isAllDay;
        private final Period period;

        public static MonthlyScheduleDTO toDTO(Schedule schedule) {
            return MonthlyScheduleDTO.builder()
                    .scheduleId(schedule.getId())
                    .title(schedule.getTitle())
                    .location(schedule.getLocation())
                    .startedAt(schedule.getStartedAt())
                    .finishedAt(schedule.getFinishedAt())
                    .isAllDay(schedule.getIsAllDay())
                    .period(schedule.getPeriod())
                    .build();
        }

        public static MonthlyScheduleDTO toDTOWithDate(Schedule schedule, LocalDateTime startedAt, LocalDateTime finishedAt) {
            return MonthlyScheduleDTO.builder()
                    .scheduleId(schedule.getId())
                    .title(schedule.getTitle())
                    .location(schedule.getLocation())
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .isAllDay(schedule.getIsAllDay())
                    .period(schedule.getPeriod())
                    .build();
        }
    }
}

package com.example.spot.service.memberstudy;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.enums.Period;
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.StudyRepository;
import com.example.spot.web.dto.study.response.ScheduleResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MemberStudyQueryServiceImpl implements MemberStudyQueryService {

    private final StudyRepository studyRepository;

    public ScheduleResponseDTO.MonthlyScheduleListDTO getMonthlySchedules(Long studyId, int year, int month) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        List<ScheduleResponseDTO.MonthlyScheduleDTO> monthlyScheduleDTOS = new ArrayList<>();

        study.getSchedules().forEach(schedule -> {
                    if (schedule.getPeriod().equals(Period.NONE)) {
                        addSchedule(schedule, year, month, monthlyScheduleDTOS);
                    } else {
                        addPeriodSchedules(schedule, year, month, monthlyScheduleDTOS);
                    }
                });

        return ScheduleResponseDTO.MonthlyScheduleListDTO.toDTO(study, monthlyScheduleDTOS);
    }

    private void addSchedule(Schedule schedule, int year, int month, List<ScheduleResponseDTO.MonthlyScheduleDTO> monthlyScheduleDTOS) {
        if (schedule.getStartedAt().getYear() == year && schedule.getStartedAt().getMonthValue() == month) {
            monthlyScheduleDTOS.add(ScheduleResponseDTO.MonthlyScheduleDTO.toDTO(schedule));
        }
    }

    private void addPeriodSchedules(Schedule schedule, int year, int month, List<ScheduleResponseDTO.MonthlyScheduleDTO> monthlyScheduleDTOS) {

        Duration duration = Duration.between(schedule.getStartedAt(), schedule.getFinishedAt()); // 일정 수행 시간
        DayOfWeek targetDayOfWeek = schedule.getStartedAt().getDayOfWeek(); // 일정을 반복할 요일
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1); // 탐색 연월의 첫째 날
        LocalDate newStartedAtDate = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(targetDayOfWeek)); // 탐색할 첫 날짜

        // 일정 시작일이 탐색 연월 내에 있는 경우에만 반복
        if (schedule.getStartedAt().isBefore(firstDayOfMonth.plusMonths(1).atStartOfDay())) {
            while (newStartedAtDate.getMonthValue() == month) {
                LocalDateTime newStartedAt = newStartedAtDate.atStartOfDay().with(schedule.getStartedAt().toLocalTime());
                LocalDateTime newFinishedAt = newStartedAt.plus(duration);

                monthlyScheduleDTOS.add(ScheduleResponseDTO.MonthlyScheduleDTO.toDTOWithDate(schedule, newStartedAt, newFinishedAt));

                if (schedule.getPeriod().equals(Period.DAILY)) {
                    newStartedAtDate = newStartedAtDate.plusDays(1);
                } else if (schedule.getPeriod().equals(Period.WEEKLY)) {
                    newStartedAtDate = newStartedAtDate.plusWeeks(1);
                } else if (schedule.getPeriod().equals(Period.BIWEEKLY)) {
                    newStartedAtDate = newStartedAtDate.plusWeeks(2);
                } else if (schedule.getPeriod().equals(Period.MONTHLY)) {
                    newStartedAtDate = newStartedAtDate.plusMonths(1);
                }
            }
        }

    }

}

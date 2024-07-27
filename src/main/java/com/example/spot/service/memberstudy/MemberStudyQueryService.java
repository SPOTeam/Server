package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.study.response.ScheduleResponseDTO;

public interface MemberStudyQueryService {

    ScheduleResponseDTO.MonthlyScheduleListDTO getMonthlySchedules(Long studyId, int year, int month);

    ScheduleResponseDTO.MonthlyScheduleDTO getSchedule(Long studyId, Long scheduleId);
}

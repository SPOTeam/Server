package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.memberstudy.response.StudyTerminationResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyWithdrawalResponseDTO;
import com.example.spot.web.dto.study.request.ScheduleRequestDTO;
import com.example.spot.web.dto.study.response.ScheduleResponseDTO;
import com.example.spot.web.dto.study.response.StudyApplyResponseDTO;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO.StudyApplyMemberDTO;

public interface MemberStudyCommandService {

    StudyWithdrawalResponseDTO.WithdrawalDTO withdrawFromStudy(Long memberId, Long studyId);

    StudyTerminationResponseDTO.TerminationDTO terminateStudy(Long studyId);

    // 스터디 신청 수락
    StudyApplyResponseDTO acceptAndRejectStudyApply(Long memberId, Long studyId, boolean isAccept);

    ScheduleResponseDTO.ScheduleDTO addSchedule(Long studyId, ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO);

    ScheduleResponseDTO.ScheduleDTO modSchedule(Long studyId, Long scheduleId, ScheduleRequestDTO.ScheduleDTO scheduleModDTO);
}

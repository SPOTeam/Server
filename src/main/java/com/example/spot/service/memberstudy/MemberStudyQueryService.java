package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.study.response.ScheduleResponseDTO;

import com.example.spot.web.dto.study.response.StudyMemberResponseDTO;
import com.example.spot.web.dto.study.response.StudyPostResponseDTO;
import com.example.spot.web.dto.study.response.StudyScheduleResponseDTO;
import org.springframework.data.domain.Pageable;

public interface MemberStudyQueryService {

    ScheduleResponseDTO.MonthlyScheduleListDTO getMonthlySchedules(Long studyId, int year, int month);

    ScheduleResponseDTO.MonthlyScheduleDTO getSchedule(Long studyId, Long scheduleId);

    // 스터디 공지 게시글 불러오기
    StudyPostResponseDTO findStudyAnnouncementPost(Long studyId);

    // 스터디 다가오는 모임 일정 불러오기
    StudyScheduleResponseDTO findStudySchedule(Long studyId, Pageable pageable);

    // 참여하는 회원 목록 불러오기
    StudyMemberResponseDTO findStudyMembers(Long studyId);

    // 스터디 별 신청 회원 목록 조회하기
    StudyMemberResponseDTO findStudyApplicants(Long studyId);

    // 스터디 신청 정보 가져오기
    StudyMemberResponseDTO.StudyApplyMemberDTO findStudyApplication(Long studyId, Long memberId);
}

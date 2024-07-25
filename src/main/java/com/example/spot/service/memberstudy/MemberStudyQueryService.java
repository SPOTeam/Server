package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.study.response.StudyMemberResponseDTO;
import com.example.spot.web.dto.study.response.StudyPostResponseDTO;
import com.example.spot.web.dto.study.response.StudyScheduleResponseDTO;
import org.springframework.data.domain.Pageable;

public interface MemberStudyQueryService {


    // 스터디 공지 게시글 불러오기
    StudyPostResponseDTO findStudyAnnouncementPost(Long studyId);

    // 스터디 다가오는 모임 일정 불러오기
    StudyScheduleResponseDTO findStudySchedule(Long studyId, Pageable pageable);

    // 참여하는 회원 목록 불러오기
    StudyMemberResponseDTO findStudyMembers(Long studyId);
}

package com.example.spot.service.memberstudy;

import com.example.spot.domain.enums.Theme;
import com.example.spot.web.dto.memberstudy.response.ScheduleResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostCommentResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostResDTO;
import com.example.spot.web.dto.memberstudy.response.StudyQuizResponseDTO;
import com.example.spot.web.dto.study.response.*;

import org.springframework.data.domain.PageRequest;
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

    // 금일 회원 출석 여부 불러오기
    StudyQuizResponseDTO.AttendanceListDTO getAllAttendances(Long studyId, Long quizId);

    // 스터디 게시글 목록 불러오기
    StudyPostResDTO.PostListDTO getAllPosts(PageRequest pageRequest, Long studyId, Theme theme);

    // 스터디 게시글 불러오기
    StudyPostResDTO.PostDetailDTO getPost(Long studyId, Long postId);

    // 스터디 게시글 댓글 목록 불러오기
    StudyPostCommentResponseDTO.CommentReplyListDTO getAllComments(Long studyId, Long postId);
}

package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO;
import com.example.spot.web.dto.memberstudy.response.*;

import com.example.spot.web.dto.study.response.*;

import java.time.LocalDate;
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

    // 스터디 투표 목록 조회
    StudyVoteResponseDTO.VoteListDTO getAllVotes(Long studyId);

    // 스터디 투표 마감 여부 조회
    Boolean getIsCompleted(Long voteId);

    // 스터디 투표(진행중) 조회
    StudyVoteResponseDTO.VoteDTO getVoteInProgress(Long studyId, Long voteId);

    // 스터디 투표(마감) 조회
    StudyVoteResponseDTO.CompletedVoteDTO getVoteInCompletion(Long studyId, Long voteId);

    // 스터디 투표 현황 조회
    StudyVoteResponseDTO.CompletedVoteDetailDTO getCompletedVoteDetail(Long studyId, Long voteId);

    // 스터디 이미지 목록 조회
    StudyImageResponseDTO.ImageListDTO getAllStudyImages(Long studyId, PageRequest pageRequest);

    // 내 투두 리스트 조회
    ToDoListResponseDTO.ToDoListSearchResponseDTO getToDoList(Long studyId, LocalDate date, PageRequest pageRequest);

    // 스터디 원 투두 리스트 조회
    ToDoListResponseDTO.ToDoListSearchResponseDTO getMemberToDoList(Long studyId, Long memberId, LocalDate date, PageRequest pageRequest);
}

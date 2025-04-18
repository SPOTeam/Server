package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.memberstudy.request.*;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListRequestDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListCreateResponseDTO;
import com.example.spot.web.dto.memberstudy.response.*;
import com.example.spot.web.dto.study.response.StudyApplyResponseDTO;
import jakarta.validation.Valid;

import java.time.LocalDate;

public interface MemberStudyCommandService {

    StudyWithdrawalResponseDTO.WithdrawalDTO withdrawFromStudy(Long studyId);
    StudyWithdrawalResponseDTO.WithdrawalDTO withdrawHostFromStudy(Long studyId, StudyHostWithdrawRequestDTO requestDTO);

    StudyTerminationResponseDTO.TerminationDTO terminateStudy(Long studyId, String performance);

    // 스터디 신청 수락
    StudyApplyResponseDTO acceptAndRejectStudyApply(Long memberId, Long studyId, boolean isAccept);

    StudyApplyResponseDTO acceptAndRejectStudyApplyForTest(Long memberId, Long studyId, boolean isAccept);

    // 일정 생성
    ScheduleResponseDTO.ScheduleDTO addSchedule(Long studyId, ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO);

    // 일정 수정
    ScheduleResponseDTO.ScheduleDTO modSchedule(Long studyId, Long scheduleId, ScheduleRequestDTO.ScheduleDTO scheduleModDTO);

    // 스터디 퀴즈 생성
    StudyQuizResponseDTO.QuizDTO createAttendanceQuiz(Long studyId, Long scheduleId, StudyQuizRequestDTO.QuizDTO quizRequestDTO);

    // 스터디 출석
    StudyQuizResponseDTO.AttendanceDTO attendantStudy(Long studyId, Long scheduleId, StudyQuizRequestDTO.AttendanceDTO attendanceRequestDTO);

    // 스터디 퀴즈 삭제
    StudyQuizResponseDTO.QuizDTO deleteAttendanceQuiz(Long studyId, Long scheduleId, LocalDate date);

    // 스터디 투표 생성
    StudyVoteResponseDTO.VotePreviewDTO createVote(Long studyId, StudyVoteRequestDTO.VoteDTO voteDTO);

    // 스터디 투표 참여
    StudyVoteResponseDTO.VotedOptionDTO vote(Long studyId, Long voteId, StudyVoteRequestDTO.VotedOptionDTO votedOptionDTO);

    // 스터디 투표 수정
    StudyVoteResponseDTO.VotePreviewDTO updateVote(Long studyId, Long voteId, StudyVoteRequestDTO.VoteUpdateDTO voteDTO);

    // 스터디 투표 삭제
    StudyVoteResponseDTO.VotePreviewDTO deleteVote(Long studyId, Long voteId);

    // 스터디 회원 신고
    MemberResponseDTO.ReportedMemberDTO reportStudyMember(Long studyId, Long memberId, @Valid StudyMemberReportDTO studyMemberReportDTO);

    // 스터디 게시글 신고
    StudyPostResDTO.PostPreviewDTO reportStudyPost(Long studyId, Long postId);

    // 투두 리스트 생성
    ToDoListCreateResponseDTO createToDoList(Long studyId, ToDoListRequestDTO.ToDoListCreateDTO toDoListCreateDTO);

    // 투두 리스트 체크
    ToDoListResponseDTO.ToDoListUpdateResponseDTO checkToDoList(Long studyId, Long toDoListId);

    // 투두 리스트 수정
    ToDoListResponseDTO.ToDoListUpdateResponseDTO updateToDoList(Long studyId, Long toDoListId, ToDoListRequestDTO.ToDoListCreateDTO toDoListCreateDTO);

    // 투두 리스트 삭제
     ToDoListResponseDTO.ToDoListUpdateResponseDTO deleteToDoList(Long studyId, Long toDoListId);
}

package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.memberstudy.request.*;
import com.example.spot.web.dto.memberstudy.response.*;
import com.example.spot.web.dto.study.response.StudyApplyResponseDTO;
import jakarta.validation.Valid;

public interface MemberStudyCommandService {

    StudyWithdrawalResponseDTO.WithdrawalDTO withdrawFromStudy(Long memberId, Long studyId);

    StudyTerminationResponseDTO.TerminationDTO terminateStudy(Long studyId);

    // 스터디 신청 수락
    StudyApplyResponseDTO acceptAndRejectStudyApply(Long memberId, Long studyId, boolean isAccept);

    // 일정 생성
    ScheduleResponseDTO.ScheduleDTO addSchedule(Long studyId, ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO);

    // 일정 수정
    ScheduleResponseDTO.ScheduleDTO modSchedule(Long studyId, Long scheduleId, ScheduleRequestDTO.ScheduleDTO scheduleModDTO);

    // 스터디 퀴즈 생성
    StudyQuizResponseDTO.QuizDTO createAttendanceQuiz(Long studyId, StudyQuizRequestDTO.QuizDTO quizRequestDTO);

    // 스터디 출석
    StudyQuizResponseDTO.AttendanceDTO attendantStudy(Long studyId, Long quizId, StudyQuizRequestDTO.AttendanceDTO attendanceRequestDTO);

    // 스터디 퀴즈 삭제
    StudyQuizResponseDTO.QuizDTO deleteAttendanceQuiz(Long studyId, Long quizId);

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
}

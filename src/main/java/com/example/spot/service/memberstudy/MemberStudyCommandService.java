package com.example.spot.service.memberstudy;

import com.example.spot.web.dto.memberstudy.request.*;
import com.example.spot.web.dto.memberstudy.response.*;
import com.example.spot.web.dto.study.response.StudyApplyResponseDTO;

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

    // 스터디 게시글 생성
    StudyPostResDTO.PostPreviewDTO createPost(Long studyId, StudyPostRequestDTO.PostDTO postRequestDTO);

    // 스터디 게시글 삭제
    StudyPostResDTO.PostPreviewDTO deletePost(Long studyId, Long postId);

    // 스터디 게시글 좋아요
    StudyPostResDTO.PostLikeNumDTO likePost(Long studyId, Long postId, Long memberId);

    // 스터디 게시글 좋아요 취소
    StudyPostResDTO.PostLikeNumDTO cancelPostLike(Long studyId, Long postId, Long memberId);

    // 스터디 게시글 댓글 생성
    StudyPostCommentResponseDTO.CommentDTO createComment(Long studyId, Long postId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO);

    // 스터디 게시글 답글 생성
    StudyPostCommentResponseDTO.CommentDTO createReply(Long studyId, Long postId, Long commentId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO);

    // 스터디 게시글 댓글 삭제 (댓/답글 구분 X)
    StudyPostCommentResponseDTO.CommentIdDTO deleteComment(Long studyId, Long postId, Long commentId);

    // 스터디 게시글 댓글 좋아요
    StudyPostCommentResponseDTO.CommentPreviewDTO likeComment(Long studyId, Long postId, Long commentId, Long memberId);

    // 스터디 게시글 댓글 싫어요
    StudyPostCommentResponseDTO.CommentPreviewDTO dislikeComment(Long studyId, Long postId, Long commentId, Long memberId);

    // 스터디 게시글 댓글 좋아요 취소
    StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentLike(Long studyId, Long postId, Long commentId, Long likeId, Long memberId);

    // 스터디 게시글 댓글 싫어요 취소
    StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentDislike(Long studyId, Long postId, Long commentId, Long dislikeId, Long memberId);

    // 스터디 투표 생성
    StudyVoteResponseDTO.VotePreviewDTO createVote(Long studyId, StudyVoteRequestDTO.VoteDTO voteDTO);

    // 스터디 투표 참여
    StudyVoteResponseDTO.VotedOptionDTO vote(Long studyId, Long voteId, StudyVoteRequestDTO.VotedOptionDTO votedOptionDTO);

    // 스터디 투표 수정
    StudyVoteResponseDTO.VotePreviewDTO updateVote(Long studyId, Long voteId, StudyVoteRequestDTO.VoteUpdateDTO voteDTO);

    // 스터디 투표 삭제
    StudyVoteResponseDTO.VotePreviewDTO deleteVote(Long studyId, Long voteId);


}

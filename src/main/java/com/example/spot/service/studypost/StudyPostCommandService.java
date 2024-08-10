package com.example.spot.service.studypost;

import com.example.spot.web.dto.memberstudy.request.StudyPostCommentRequestDTO;
import com.example.spot.web.dto.memberstudy.request.StudyPostRequestDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostCommentResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostResDTO;

public interface StudyPostCommandService {

    // 스터디 게시글 생성
    StudyPostResDTO.PostPreviewDTO createPost(Long studyId, StudyPostRequestDTO.PostDTO postRequestDTO);

    // 스터디 게시글 삭제
    StudyPostResDTO.PostPreviewDTO deletePost(Long studyId, Long postId);

    // 스터디 게시글 좋아요
    StudyPostResDTO.PostLikeNumDTO likePost(Long studyId, Long postId);

    // 스터디 게시글 좋아요 취소
    StudyPostResDTO.PostLikeNumDTO cancelPostLike(Long studyId, Long postId);

    // 스터디 게시글 댓글 생성
    StudyPostCommentResponseDTO.CommentDTO createComment(Long studyId, Long postId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO);

    // 스터디 게시글 답글 생성
    StudyPostCommentResponseDTO.CommentDTO createReply(Long studyId, Long postId, Long commentId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO);

    // 스터디 게시글 댓글 삭제 (댓/답글 구분 X)
    StudyPostCommentResponseDTO.CommentIdDTO deleteComment(Long studyId, Long postId, Long commentId);

    // 스터디 게시글 댓글 좋아요
    StudyPostCommentResponseDTO.CommentPreviewDTO likeComment(Long studyId, Long postId, Long commentId);

    // 스터디 게시글 댓글 싫어요
    StudyPostCommentResponseDTO.CommentPreviewDTO dislikeComment(Long studyId, Long postId, Long commentId);

    // 스터디 게시글 댓글 좋아요 취소
    StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentLike(Long studyId, Long postId, Long commentId);

    // 스터디 게시글 댓글 싫어요 취소
    StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentDislike(Long studyId, Long postId, Long commentId);

}

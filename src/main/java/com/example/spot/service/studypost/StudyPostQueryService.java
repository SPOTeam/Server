package com.example.spot.service.studypost;

import com.example.spot.domain.enums.ThemeQuery;
import com.example.spot.web.dto.memberstudy.response.StudyPostCommentResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostResDTO;
import org.springframework.data.domain.PageRequest;

public interface StudyPostQueryService {

    // 스터디 게시글 목록 불러오기
    StudyPostResDTO.PostListDTO getAllPosts(PageRequest pageRequest, Long studyId, ThemeQuery themeQuery);

    // 스터디 게시글 불러오기
    StudyPostResDTO.PostDetailDTO getPost(Long studyId, Long postId, Boolean likeOrScrap);

    // 스터디 게시글 댓글 목록 불러오기
    StudyPostCommentResponseDTO.CommentReplyListDTO getAllComments(Long studyId, Long postId);
}

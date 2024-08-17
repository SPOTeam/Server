package com.example.spot.service.studypost;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.enums.ThemeQuery;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.domain.study.StudyPostComment;
import com.example.spot.repository.*;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.memberstudy.response.StudyPostCommentResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyPostQueryServiceImpl implements StudyPostQueryService {

    private final StudyPostCommentRepository studyPostCommentRepository;
    private final StudyLikedPostRepository studyLikedPostRepository;
    @Value("${cloud.aws.default-image}")
    private String defaultImage;

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final StudyPostRepository studyPostRepository;
    private final MemberStudyRepository memberStudyRepository;

/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    @Override
    public StudyPostResDTO.PostListDTO getAllPosts(PageRequest pageRequest, Long studyId, ThemeQuery themeQuery) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        //=== Feature ===//

        List<StudyPost> studyPosts;
        if (themeQuery == null) {
            // query가 없는 경우
            studyPosts = studyPostRepository.findAllByStudyId(studyId, pageRequest);
        } else if (themeQuery.equals(ThemeQuery.ANNOUNCEMENT)) {
            // query가 ANNOUNCEMENT인 경우
            studyPosts = studyPostRepository.findAllByStudyIdAndIsAnnouncement(studyId, Boolean.TRUE, pageRequest);
        } else {
            // query가 스터디 테마인 경우
            Theme theme = themeQuery.toTheme();
            studyPosts = studyPostRepository.findAllByStudyIdAndTheme(studyId, theme, pageRequest);
        }

        return StudyPostResDTO.PostListDTO.toDTO(study, studyPosts.stream()
                .map(studyPost -> {
                    if (studyLikedPostRepository.existsByMemberIdAndStudyPostId(memberId, studyPost.getId())) {
                        return StudyPostResDTO.PostDTO.toDTO(studyPost, true);
                    } else {
                        return StudyPostResDTO.PostDTO.toDTO(studyPost, false);
                    }
                })
                .toList());

    }

    @Override
    @Transactional(readOnly = false)
    public StudyPostResDTO.PostDetailDTO getPost(Long studyId, Long postId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        StudyPost studyPost = studyPostRepository.findById(postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        //=== Feature ===//
        studyPost.plusHitNum();
        studyPost = studyPostRepository.save(studyPost);
        memberRepository.save(member);
        studyRepository.save(study);

        boolean isLiked = studyLikedPostRepository.existsByMemberIdAndStudyPostId(memberId, studyPost.getId());
        return StudyPostResDTO.PostDetailDTO.toDTO(studyPost, isLiked);
    }

/* ----------------------------- 스터디 게시글 댓글 관련 API ------------------------------------- */

    @Override
    public StudyPostCommentResponseDTO.CommentReplyListDTO getAllComments(Long studyId, Long postId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        StudyPost studyPost = studyPostRepository.findById(postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        //=== Feature ===//
        List<StudyPostComment> studyPostComments = studyPostCommentRepository.findByStudyPostId(studyPost.getId()).stream()
                .filter(studyPostComment -> studyPostComment.getParentComment() == null)
                .sorted(Comparator.comparing(StudyPostComment::getCreatedAt))
                .toList();

        return StudyPostCommentResponseDTO.CommentReplyListDTO.toDTO(studyPost.getId(), studyPostComments, member, defaultImage);
    }

}

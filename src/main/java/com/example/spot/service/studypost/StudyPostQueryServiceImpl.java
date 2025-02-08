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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyPostQueryServiceImpl implements StudyPostQueryService {

    private final StudyPostCommentRepository studyPostCommentRepository;
    private final StudyLikedPostRepository studyLikedPostRepository;
    @Value("${image.post.anonymous.profile}")
    private String defaultImage;

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final StudyPostRepository studyPostRepository;
    private final MemberStudyRepository memberStudyRepository;

/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    /**
     * 특정 테마(카테고리)에 속한 스터디 게시글 목록을 조회하는 메서드입니다.
     * 오프셋 기반 페이징이 적용되어 있습니다.
     * @param pageRequest 페이징에 필요한 페이지 번호와 페이지 사이즈 정보를 입력 받습니다.
     * @param studyId 게시글 목록을 조회할 타겟 스터디의 아이디를 입력 받습니다.
     * @param themeQuery 게시글 테마를 입력 받습니다. themeQuery는 null일 수 있습니다.
     * @return 조건에 맞는 스터디 게시글 목록을 반환합니다.
     *          1. themeQuery가 있는 경우 해당 테마의 게시글 목록을 반환합니다.
     *          2. themeQuery가 null인 경우 필터링 없이 게시글 목록을 반환합니다.
     */
    @Override
    public StudyPostResDTO.PostListDTO getAllPosts(PageRequest pageRequest, Long studyId, ThemeQuery themeQuery) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

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

    /**
     * 스터디 게시판의 특정 게시글을 조회하는 메서드입니다.
     * @param studyId 게시글을 조회할 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 조회할 타겟 게시글의 아이디를 입력 받습니다.
     * @return 스터디 게시글의 정보를 반환합니다.
     */
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

        Integer commentNum = studyPostCommentRepository.findAllByStudyPostId(postId).size();
        boolean isLiked = studyLikedPostRepository.existsByMemberIdAndStudyPostId(memberId, studyPost.getId());

        return StudyPostResDTO.PostDetailDTO.toDTO(studyPost, commentNum, isLiked);
    }

/* ----------------------------- 스터디 게시글 댓글 관련 API ------------------------------------- */

    /**
     * 특정 스터디 게시글의 모든 댓글을 조회하는 메서드입니다.
     * @param studyId 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 댓글이 작성된 게시글의 아이디를 입력 받습니다.
     * @return 스터디 게시글에 작성된 댓글의 목록을 반환합니다. 하나의 댓글에는 해당 댓글에 대한 답글 목록이 포함되어 있습니다.
     */
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
        List<StudyPostComment> studyPostComments = studyPostCommentRepository.findAllByStudyPostId(studyPost.getId()).stream()
                .filter(studyPostComment -> studyPostComment.getParentComment() == null)
                .sorted(Comparator.comparing(StudyPostComment::getCreatedAt))
                .toList();

        return StudyPostCommentResponseDTO.CommentReplyListDTO.toDTO(studyPost.getId(), studyPostComments, member, defaultImage);
    }

}

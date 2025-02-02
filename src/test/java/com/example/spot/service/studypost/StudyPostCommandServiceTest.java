package com.example.spot.service.studypost;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.StudyLikedComment;
import com.example.spot.domain.mapping.StudyLikedPost;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.domain.study.StudyPostComment;
import com.example.spot.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyPostCommandServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StudyRepository studyRepository;
    @Mock
    private MemberStudyRepository memberStudyRepository;

    @Mock
    private StudyPostRepository studyPostRepository;
    @Mock
    private StudyLikedPostRepository studyLikedPostRepository;

    @Mock
    private StudyPostCommentRepository studyPostCommentRepository;

    @InjectMocks
    private StudyPostQueryServiceImpl studyPostQueryService;

    private static PageRequest pageRequest;

    private static Study study;
    private static Member member1;
    private static Member member2;
    private static Member owner;
    private static MemberStudy member1Study;
    private static MemberStudy ownerStudy;

    private static StudyPost studyPost1;
    private static StudyPost studyPost2;
    private static StudyPost studyPost3;
    private static StudyLikedPost studyLikedPost;
    private static StudyPostComment studyPost1Comment1;
    private static StudyPostComment studyPost1Comment2;
    private static StudyLikedComment studyLikedComment;
    private static StudyLikedComment studyDislikedComment;

    @BeforeEach
    void setUp() {
        initMember();
        initStudy();
        initMemberStudy();
        initStudyPost();
        initStudyLikedPost();
        initStudyPostComment();
        initStudyLikedComment();

        // Member
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(owner));

        // Study
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));

        // MemberStudy
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(1L, 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(member1Study));
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(2L, 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.empty());
        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(3L, 1L, ApplicationStatus.APPROVED))
                .thenReturn(Optional.of(ownerStudy));

        // StudyPost
        when(studyPostRepository.findById(1L)).thenReturn(Optional.of(studyPost1));
        when(studyPostRepository.findById(2L)).thenReturn(Optional.of(studyPost2));
        when(studyPostRepository.findById(3L)).thenReturn(Optional.of(studyPost3));

        when(studyLikedPostRepository.findByMemberIdAndStudyPostId(3L, 1L))
                .thenReturn(Optional.of(studyLikedPost));
        when(studyLikedPostRepository.existsByMemberIdAndStudyPostId(3L, 1L))
                .thenReturn(true);

        // Comment
        when(studyPostCommentRepository.findAllByStudyPostId(1L))
                .thenReturn(List.of(studyPost1Comment1, studyPost1Comment2));

    }

/*-------------------------------------------------------- 게시글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 작성 - (성공)")
    void createPost_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 작성 - 스터디 회원이 아닌 경우 (실패)")
    void createPost_NotStudyMember_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 작성 - 제목이 50자를 초과하는 경우 (실패)")
    void createPost_TitleOverflow_Fail() {
    }


/*-------------------------------------------------------- 게시글 삭제 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 삭제 - (성공)")
    void deletePost_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 삭제 - 이미 삭제된 게시글인 경우 (실패)")
    void deletePost_AlreadyDeleted_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 삭제 - 작성자 본인이나 스터디장이 아닌 경우 (실패)")
    void deletePost_NotAvailableMember_Fail() {
    }

/*-------------------------------------------------------- 게시글 좋아요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 좋아요 - (성공)")
    void likePost_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 좋아요 - 스터디 회원이 아닌 경우 (실패)")
    void likePost_NotStudyMember_Fail() {
    }
    @Test
    @DisplayName("스터디 게시글 좋아요 - 이미 좋아요를 누른 경우 (실패)")
    void likePost_AlreadyLiked_Fail() {
    }


/*-------------------------------------------------------- 게시글 좋아요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 좋아요 취소 - (성공)")
    void cancelPostLike_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 좋아요 취소 - 스터디 회원이 아닌 경우 (실패)")
    void cancelPostLike_NotStudyMember_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 좋아요 취소 - 좋아요를 누르지 않은 게시글인 경우 (실패)")
    void cancelPostLike_NotLiked_Fail() {
    }


/*-------------------------------------------------------- 댓글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 작성 - (성공)")
    void createComment_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 작성 - 스터디 회원이 아닌 경우 (실패)")
    void createComment_NotStudyMember_Fail() {
    }



/*-------------------------------------------------------- 답글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 답글 작성 - (성공)")
    void createReply_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 답글 작성 - 스터디 회원이 아닌 경우 (실패)")
    void createReply_NotStudyMember_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 답글 작성 - 상위 댓글이 존재하지 않는 경우 (실패)")
    void createReply_ParentCommentNotExist_Fail() {
    }


/*-------------------------------------------------------- 댓글 삭제 ------------------------------------------------------------------------*/

    // @Test
    // @DisplayName("스터디 게시글 댓글 삭제")
    // void deleteComment() {
    // }

/*-------------------------------------------------------- 댓글 좋아요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 - (성공)")
    void likeComment_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 - 스터디 회원이 아닌 경우 (실패)")
    void likeComment_NotStudyMember_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 - 이미 좋아요를 누른 경우 (실패)")
    void likeComment_AlreadyLiked_Fail() {
    }


/*-------------------------------------------------------- 댓글 싫어요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 - (성공)")
    void dislikeComment_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 - 스터디 회원인 아닌 경우(실패)")
    void dislikeComment_NotStudyMember_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 - 이미 싫어요를 누른 경우(실패)")
    void dislikeComment_AlreadyDisliked_Fail() {
    }

/*-------------------------------------------------------- 댓글 좋아요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 취소 - (성공)")
    void cancelCommentLike_Success() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 취소 - 스터디 회원이 아닌 경우 (실패)")
    void cancelCommentLike_NotStudyMember_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 취소 - 좋아요를 누른 댓글이 아닌 경우 (실패)")
    void cancelCommentLike_NotLiked_Fail() {
    }

/*-------------------------------------------------------- 댓글 싫어요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 취소 - (성공)")
    void cancelCommentDislike() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 취소 - 스터디 회원이 아닌 경우 (실패)")
    void cancelCommentDislike_NotStudyMember_Fail() {
    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 취소 - 싫어요를 누른 댓글이 아닌 경우 (실패)")
    void cancelCommentDislike_NotDisliked_Fail() {
    }


/*-------------------------------------------------------- Utils ------------------------------------------------------------------------*/

    private static void getAuthentication(Long memberId) {
        String idString = String.valueOf(memberId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(idString, null, Collections.emptyList());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private static void initMember() {
        member1 = Member.builder()
                .id(1L)
                .studyPostList(new ArrayList<>())
                .studyLikedPostList(new ArrayList<>())
                .studyPostCommentList(new ArrayList<>())
                .studyLikedCommentList(new ArrayList<>())
                .build();
        member2 = Member.builder()
                .id(2L)
                .studyPostList(new ArrayList<>())
                .studyLikedPostList(new ArrayList<>())
                .studyPostCommentList(new ArrayList<>())
                .studyLikedCommentList(new ArrayList<>())
                .build();
        owner = Member.builder()
                .id(3L)
                .studyPostList(new ArrayList<>())
                .studyLikedPostList(new ArrayList<>())
                .studyPostCommentList(new ArrayList<>())
                .studyLikedCommentList(new ArrayList<>())
                .build();
    }

    private static void initStudy() {
        study = Study.builder()
                .id(1L)
                .gender(Gender.MALE)
                .minAge(20)
                .maxAge(29)
                .fee(10000)
                .profileImage("a.jpg")
                .hasFee(true)
                .isOnline(true)
                .goal("SQLD")
                .introduction("SQLD 자격증 스터디")
                .title("SQLD Master")
                .maxPeople(10L)
                .build();
    }

    private static void initMemberStudy() {
        ownerStudy = MemberStudy.builder()
                .id(1L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(true)
                .introduction("Hi")
                .member(owner)
                .study(study)
                .build();
        member1Study = MemberStudy.builder()
                .id(2L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(false)
                .introduction("Hi")
                .member(member1)
                .study(study)
                .build();
    }

    private static void initStudyPost() {
        studyPost1 = StudyPost.builder()
                .id(1L)
                .member(member1)
                .study(study)
                .isAnnouncement(false)
                .theme(Theme.FREE_TALK)
                .title("잡담")
                .content("내용")
                .hitNum(0)
                .likeNum(0)
                .commentNum(0)
                .build();
        member1.addStudyPost(studyPost1);
        study.addStudyPost(studyPost1);

        studyPost2 = StudyPost.builder()
                .id(2L)
                .member(owner)
                .study(study)
                .isAnnouncement(true)
                .theme(Theme.INFO_SHARING)
                .title("공지")
                .content("내용")
                .hitNum(0)
                .likeNum(0)
                .commentNum(0)
                .build();
        owner.addStudyPost(studyPost2);
        study.addStudyPost(studyPost2);

        studyPost3 = StudyPost.builder()
                .id(3L)
                .member(owner)
                .study(study)
                .isAnnouncement(false)
                .theme(Theme.FREE_TALK)
                .title("테스트")
                .content("내용")
                .hitNum(0)
                .likeNum(0)
                .commentNum(0)
                .build();
        owner.addStudyPost(studyPost3);
        study.addStudyPost(studyPost3);

        for (int i=0; i<10; i++) {
            studyPost1.plusHitNum();
        }
    }

    private static void initStudyLikedPost() {
        studyLikedPost = StudyLikedPost.builder()
                .id(1L)
                .studyPost(studyPost1)
                .member(owner)
                .build();
        studyPost1.addLikedPost(studyLikedPost);
        studyPost1.plusLikeNum();
        member1.addStudyLikedPost(studyLikedPost);
    }

    private static void initStudyPostComment() {
        studyPost1Comment1 = StudyPostComment.builder()
                .id(1L)
                .studyPost(studyPost1)
                .member(member1)
                .content("댓글")
                .likeCount(0)
                .dislikeCount(0)
                .isAnonymous(true)
                .isDeleted(false)
                .parentComment(null)
                .build();
        studyPost1Comment2 = StudyPostComment.builder()
                .id(2L)
                .studyPost(studyPost1)
                .member(owner)
                .content("답글")
                .likeCount(0)
                .dislikeCount(0)
                .isAnonymous(false)
                .isDeleted(false)
                .parentComment(studyPost1Comment1)
                .build();
        studyPost1Comment1.addChildrenComment(studyPost1Comment2);

        studyPost1.addComment(studyPost1Comment1);
        studyPost1.addComment(studyPost1Comment2);
    }

    private static void initStudyLikedComment() {
        studyLikedComment = StudyLikedComment.builder()
                .id(1L)
                .isLiked(true)
                .studyPostComment(studyPost1Comment2)
                .member(member1)
                .build();
        studyPost1Comment2.addLikedComment(studyLikedComment);
        member1.addStudyLikedComment(studyLikedComment);

        studyDislikedComment = StudyLikedComment.builder()
                .id(2L)
                .isLiked(false)
                .studyPostComment(studyPost1Comment2)
                .member(owner)
                .build();
        studyPost1Comment2.addLikedComment(studyLikedComment);
        owner.addStudyLikedComment(studyLikedComment);
    }

}
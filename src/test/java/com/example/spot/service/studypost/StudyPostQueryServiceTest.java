package com.example.spot.service.studypost;

import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.enums.ThemeQuery;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.StudyLikedComment;
import com.example.spot.domain.mapping.StudyLikedPost;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.domain.study.StudyPostComment;
import com.example.spot.repository.*;
import com.example.spot.web.dto.memberstudy.response.StudyPostCommentResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostResDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyPostQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StudyRepository studyRepository;
    @Mock
    private MemberStudyRepository memberStudyRepository;

    @Mock
    private StudyPostRepository studyPostRepository;
    @Mock
    private StudyPostImageRepository studyPostImageRepository;
    @Mock
    private StudyPostCommentRepository studyPostCommentRepository;

    @Mock
    private StudyLikedPostRepository studyLikedPostRepository;
    @Mock
    private StudyLikedCommentRepository studyLikedCommentRepository;

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

/*-------------------------------------------------------- 게시글 목록 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 목록 조회 - 전체 게시글 조회 (성공)")
    void getAllPosts_All_Success() {

        // given
        Long studyId = 1L;
        Long memberId = 1L;

        getAuthentication(memberId);

        pageRequest = PageRequest.of(0, 10);
        when(studyPostRepository.findAllByStudyId(studyId, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost2, studyPost3));
        when(studyPostRepository.findAllByStudyIdAndIsAnnouncement(studyId, true, pageRequest))
                .thenReturn(List.of(studyPost2));
        when(studyPostRepository.findAllByStudyIdAndTheme(studyId, Theme.FREE_TALK, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost3));

        // when
        StudyPostResDTO.PostListDTO result = studyPostQueryService.getAllPosts(pageRequest, studyId, null);

        // then
        assertNotNull(result);
        assertThat(result.getPosts()).isNotEmpty();
        assertThat(result.getPosts()).size().isLessThanOrEqualTo(10);
        assertThat(result.getPosts()).size().isEqualTo(3);
        assertThat(result.getStudyId()).isEqualTo(studyId);
    }

    @Test
    @DisplayName("스터디 게시글 목록 조회 - 테마별 조회 (성공)")
    void getAllPosts_Theme_Success() {

        // given
        Long studyId = 1L;
        Long memberId = 1L;

        getAuthentication(memberId);

        pageRequest = PageRequest.of(0, 10);
        when(studyPostRepository.findAllByStudyId(studyId, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost2, studyPost3));
        when(studyPostRepository.findAllByStudyIdAndIsAnnouncement(studyId, true, pageRequest))
                .thenReturn(List.of(studyPost2));
        when(studyPostRepository.findAllByStudyIdAndTheme(studyId, Theme.FREE_TALK, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost3));

        // when
        StudyPostResDTO.PostListDTO result = studyPostQueryService.getAllPosts(pageRequest, studyId, ThemeQuery.FREE_TALK);

        // then
        assertNotNull(result);
        assertThat(result.getPosts()).isNotEmpty();
        assertThat(result.getPosts()).size().isLessThanOrEqualTo(10);
        assertThat(result.getPosts()).size().isEqualTo(2);
        assertThat(result.getStudyId()).isEqualTo(studyId);
    }

    @Test
    @DisplayName("스터디 게시글 목록 조회 - 공지 조회 (성공)")
    void getAllPosts_Announcements_Success() {

        // given
        Long studyId = 1L;
        Long memberId = 1L;

        getAuthentication(memberId);

        pageRequest = PageRequest.of(0, 10);
        when(studyPostRepository.findAllByStudyId(studyId, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost2, studyPost3));
        when(studyPostRepository.findAllByStudyIdAndIsAnnouncement(studyId, true, pageRequest))
                .thenReturn(List.of(studyPost2));
        when(studyPostRepository.findAllByStudyIdAndTheme(studyId, Theme.FREE_TALK, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost3));

        // when
        StudyPostResDTO.PostListDTO result = studyPostQueryService.getAllPosts(pageRequest, studyId, ThemeQuery.ANNOUNCEMENT);

        // then
        assertNotNull(result);
        assertThat(result.getPosts()).isNotEmpty();
        assertThat(result.getPosts()).size().isLessThanOrEqualTo(10);
        assertThat(result.getPosts()).size().isEqualTo(1);
        assertThat(result.getStudyId()).isEqualTo(studyId);
    }

    @Test
    @DisplayName("스터디 게시글 목록 조회 - 스터디 회원이 아닌 경우(실패)")
    void getAllPosts_NotStudyMember_Fail() {

        // given
        Long studyId = 1L;
        Long memberId = 2L;

        getAuthentication(memberId);

        pageRequest = PageRequest.of(0, 10);
        when(studyPostRepository.findAllByStudyId(studyId, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost2, studyPost3));
        when(studyPostRepository.findAllByStudyIdAndIsAnnouncement(studyId, true, pageRequest))
                .thenReturn(List.of(studyPost2));
        when(studyPostRepository.findAllByStudyIdAndTheme(studyId, Theme.FREE_TALK, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost3));

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostQueryService.getAllPosts(pageRequest, studyId, null));
    }

    @Test
    @DisplayName("스터디 게시글 목록 조회 - 존재하는 카테고리가 아닌 경우(실패)")
    void getAllPosts_NotCategorized_Fail() {

        // given
        Long studyId = 1L;
        Long memberId = 1L;

        getAuthentication(memberId);

        pageRequest = PageRequest.of(0, 10);
        when(studyPostRepository.findAllByStudyId(studyId, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost2, studyPost3));
        when(studyPostRepository.findAllByStudyIdAndIsAnnouncement(studyId, true, pageRequest))
                .thenReturn(List.of(studyPost2));
        when(studyPostRepository.findAllByStudyIdAndTheme(studyId, Theme.FREE_TALK, pageRequest))
                .thenReturn(List.of(studyPost1, studyPost3));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> studyPostQueryService.getAllPosts(pageRequest, studyId, ThemeQuery.valueOf("Nothing")));
    }


/*-------------------------------------------------------- 게시글 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 단건 조회 - (성공)")
    void getPost_Success() {

        // given
        Long studyId = 1L;
        Long memberId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostRepository.save(studyPost1)).thenReturn(studyPost1);
        when(memberRepository.save(member1)).thenReturn(member1);
        when(studyRepository.save(study)).thenReturn(study);
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1, studyPost1Comment2));
        when(studyLikedPostRepository.existsByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(false);

        // when
        StudyPostResDTO.PostDetailDTO result = studyPostQueryService.getPost(studyId, postId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getHitNum()).isEqualTo(11);
        assertThat(result.getTitle()).isEqualTo("잡담");
        assertThat(result.getCommentNum()).isEqualTo(2);
        assertThat(result.getIsLiked()).isEqualTo(false);

    }

    @Test
    @DisplayName("스터디 게시글 단건 조회 - 스터디 회원이 아닌 경우(실패)")
    void getPost_NotStudyMember_Fail() {

        // given
        Long studyId = 1L;
        Long memberId = 2L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostRepository.save(studyPost1)).thenReturn(studyPost1);
        when(memberRepository.save(member1)).thenReturn(member1);
        when(studyRepository.save(study)).thenReturn(study);
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1, studyPost1Comment2));
        when(studyLikedPostRepository.existsByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(false);

        // when & then
        assertThrows(StudyHandler.class, () ->studyPostQueryService.getPost(studyId, postId));
    }

    @Test
    @DisplayName("스터디 게시글 단건 조회 - 스터디 게시글이 아닌 경우(실패)")
    void getPost_NotStudyPost_Fail() {

        // given
        Long studyId = 1L;
        Long memberId = 2L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.empty());
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () ->studyPostQueryService.getPost(studyId, postId));
    }

/*-------------------------------------------------------- 댓글 목록 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 목록 조회 - (성공)")
    void getAllComments_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1, studyPost1Comment2));

        // when
        StudyPostCommentResponseDTO.CommentReplyListDTO result = studyPostQueryService.getAllComments(studyId, postId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getComments()).size().isEqualTo(1);
        result.getComments()
                .forEach(comment -> {
                    assertThat(comment.getCommentId()).isEqualTo(1L);       // 댓글 1개
                    assertThat(comment.getApplies()).size().isEqualTo(1L);  // 답글 1개
                });
    }

    @Test
    @DisplayName("스터디 게시글 댓글 목록 조회 - 스터디 회원이 아닌 경우(실패)")
    void getAllComments_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1, studyPost1Comment2));

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostQueryService.getAllComments(studyId, postId));
    }

    @Test
    @DisplayName("스터디 게시글 댓글 목록 조회 - 스터디 게시글이 아닌 경우(실패)")
    void getAllComments_NotStudyPost_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.empty());
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of());

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostQueryService.getAllComments(studyId, postId));
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
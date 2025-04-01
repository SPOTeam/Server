package com.example.spot.service.post;

import com.example.spot.domain.*;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.mapping.MemberScrap;
import com.example.spot.repository.*;
import com.example.spot.web.dto.post.PostSingleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private MemberScrapRepository memberScrapRepository;

    @Mock
    private LikedPostRepository likedPostRepository;

    @Mock
    private LikedPostCommentRepository likedPostCommentRepository;

    @Mock
    private LikedPostQueryService likedPostQueryService;

    @Mock
    private LikedPostCommentQueryService likedPostCommentQueryService;

    @InjectMocks
    private PostQueryServiceImpl postQueryService;


    private static Member member1;
    private static Member member2;

    private static Post post1;
    private static Post post2;
    private static PostComment post1Comment1;
    private static PostComment post1Comment2;
    private static LikedPost member1LikedPost2;
    private static LikedPost member2LikedPost1;
    private static LikedPostComment member1LikedComment1;
    private static LikedPostComment member2LikedComment1;

    private static MemberScrap member1Scrap2;
    private static MemberScrap member2Scrap1;

    private static Pageable pageable;
    private static Page<MemberScrap> memberScrapPage;


    @BeforeEach
    void setUp() {
        initMember();
        initPost();
        initPostComment();
        initLikedPost();
        initLikedPostComment();
        initMemberScrap();

        // Member
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));

        // Post
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(postRepository.findById(2L)).thenReturn(Optional.of(post2));

        // PostComment
        when(postCommentRepository.findById(1L)).thenReturn(Optional.of(post1Comment1));
        when(postCommentRepository.findById(2L)).thenReturn(Optional.of(post1Comment2));
        when(postCommentRepository.findCommentsByPostId(1L))
                .thenReturn(List.of(post1Comment1, post1Comment2));

        // LikedPost
        when(likedPostRepository.countByPostId(1L)).thenReturn(1L);
        when(likedPostRepository.countByPostId(2L)).thenReturn(1L);
        when(likedPostRepository.existsByMemberIdAndPostId(1L, 1L)).thenReturn(false);
        when(likedPostRepository.existsByMemberIdAndPostId(1L, 2L)).thenReturn(true);
        when(likedPostRepository.existsByMemberIdAndPostId(2L, 1L)).thenReturn(true);
        when(likedPostRepository.existsByMemberIdAndPostId(2L, 2L)).thenReturn(false);
        when(likedPostQueryService.countByPostId(1L)).thenReturn(1L);
        when(likedPostQueryService.countByPostId(2L)).thenReturn(1L);

        // LikedPostComment
        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(1L, 1L))
                .thenReturn(Optional.empty());
        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(2L, 1L))
                .thenReturn(Optional.of(member2LikedComment1));
        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(1L, 1L))
                .thenReturn(Optional.of(member1LikedComment1));
        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(2L, 1L))
                .thenReturn(Optional.empty());
        when(likedPostCommentRepository.countByPostCommentIdAndIsLikedTrue(1L)).thenReturn(1L);
        when(likedPostCommentRepository.countByPostCommentIdAndIsLikedTrue(2L)).thenReturn(0L);
        when(likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(1L)).thenReturn(1L);
        when(likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(2L)).thenReturn(0L);
        when(likedPostCommentRepository.existsByMemberIdAndPostCommentIdAndIsLikedTrue(1L, 1L)).thenReturn(true);
        when(likedPostCommentRepository.existsByMemberIdAndPostCommentIdAndIsLikedTrue(2L, 1L)).thenReturn(false);
        when(likedPostCommentRepository.existsByMemberIdAndPostCommentIdAndIsLikedFalse(1L, 1L)).thenReturn(false);
        when(likedPostCommentRepository.existsByMemberIdAndPostCommentIdAndIsLikedFalse(2L, 1L)).thenReturn(true);

        // MemberScrap
        when(memberScrapRepository.countByPostId(1L)).thenReturn(1L);
        when(memberScrapRepository.countByPostId(2L)).thenReturn(1L);
        when(memberScrapRepository.findByMemberId(1L, pageable)).thenReturn(memberScrapPage);
        when(memberScrapRepository.findByMemberIdAndPostId(1L, 2L))
                .thenReturn(Optional.of(member1Scrap2));
        when(memberScrapRepository.findByMemberIdAndPostId(2L, 1L))
                .thenReturn(Optional.of(member2Scrap1));
        when(memberScrapRepository.findByMemberIdAndPost_Board(1L, Board.FREE_TALK, pageable))
                .thenReturn(memberScrapPage);
        when(memberScrapRepository.findByMemberIdAndPost_Board(2L, Board.INFORMATION_SHARING, pageable))
                .thenReturn(memberScrapPage);
        when(memberScrapRepository.existsByMemberIdAndPostId(1L, 2L)).thenReturn(true);
        when(memberScrapRepository.existsByMemberIdAndPostId(2L, 1L)).thenReturn(true);
    }

    @Test
    @DisplayName("게시글 단건 조회 - 일반 조회 (성공)")
    void getPostById_Common_Success() {

        // given
        Long memberId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(likedPostQueryService.existsByMemberIdAndPostId(postId)).thenReturn(false);

        // when
        PostSingleResponse result = postQueryService.getPostById(postId, false);

        // then
        assertNotNull(result);
        assertThat(result.getWriter()).isEqualTo("익명");
        assertThat(result.getAnonymous()).isEqualTo(true);
        assertThat(result.getScrapCount()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("게시글1");
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getCommentCount()).isEqualTo(2);
        assertThat(result.getViewCount()).isEqualTo(2L);
        assertThat(result.getLikedByCurrentUser()).isEqualTo(false);
        assertThat(result.getScrapedByCurrentUser()).isEqualTo(false);
        assertThat(result.getCreatedByCurrentUser()).isEqualTo(true);
        assertThat(result.getCommentResponses().getComments().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글 단건 조회 - 스크랩 조회 (성공)")
    void getPostById_Scrap_Success() {

        // given
        Long memberId = 1L;
        Long postId = 2L;

        getAuthentication(memberId);

        when(likedPostQueryService.existsByMemberIdAndPostId(postId)).thenReturn(true);

        // when
        PostSingleResponse result = postQueryService.getPostById(postId, true);

        // then
        assertNotNull(result);
        assertThat(result.getWriter()).isEqualTo("회원2");
        assertThat(result.getAnonymous()).isEqualTo(false);
        assertThat(result.getScrapCount()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("게시글2");
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getCommentCount()).isEqualTo(0);
        assertThat(result.getViewCount()).isEqualTo(1L);
        assertThat(result.getLikedByCurrentUser()).isEqualTo(true);
        assertThat(result.getScrapedByCurrentUser()).isEqualTo(true);
        assertThat(result.getCreatedByCurrentUser()).isEqualTo(false);
        assertThat(result.getCommentResponses().getComments().size()).isEqualTo(0);
    }

    @Test
    void getPagingPosts() {
    }

    @Test
    void getPostBest() {
    }

    @Test
    void getRepresentativePosts() {
    }

    @Test
    void getPostAnnouncements() {
    }

    @Test
    void getCommentsByPostId() {
    }

    @Test
    void getScrapPagingPost() {
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
                .nickname("회원1")
                .postList(new ArrayList<>())
                .likedPostList(new ArrayList<>())
                .memberScrapList(new ArrayList<>())
                .postCommentList(new ArrayList<>())
                .likedCommentList(new ArrayList<>())
                .build();
        member2 = Member.builder()
                .id(2L)
                .nickname("회원2")
                .postList(new ArrayList<>())
                .likedPostList(new ArrayList<>())
                .memberScrapList(new ArrayList<>())
                .postCommentList(new ArrayList<>())
                .likedCommentList(new ArrayList<>())
                .build();
    }

    private static void initPost() {
        post1 = Post.builder()
                .id(1L)
                .title("게시글1")
                .isAnonymous(true)
                .commentNum(2)
                .hitNum(1)
                .scrapNum(1)
                .isAdmin(false)
                .board(Board.FREE_TALK)
                .member(member1)
                .build();
        post2 = Post.builder()
                .id(2L)
                .title("게시글2")
                .isAnonymous(false)
                .commentNum(0)
                .hitNum(1)
                .scrapNum(1)
                .isAdmin(false)
                .board(Board.INFORMATION_SHARING)
                .member(member2)
                .build();
    }

    private static void initPostComment() {
        post1Comment1 = PostComment.builder()
                .id(1L)
                .isAnonymous(true)
                .content("댓글1")
                .likeNum(1)
                .disLikeNum(1)
                .post(post1)
                .member(member1)
                .parentComment(null)
                .build();
        post1Comment2 = PostComment.builder()
                .id(2L)
                .isAnonymous(false)
                .content("댓글2")
                .likeNum(0)
                .disLikeNum(0)
                .post(post1)
                .member(member2)
                .parentComment(post1Comment1)
                .build();
    }

    private static void initLikedPost() {
        member1LikedPost2 = LikedPost.builder()
                .id(1L)
                .post(post2)
                .member(member1)
                .build();
        member2LikedPost1 = LikedPost.builder()
                .id(2L)
                .post(post1)
                .member(member2)
                .build();
    }

    private static void initLikedPostComment() {
        member1LikedComment1 = LikedPostComment.builder()
                .id(1L)
                .isLiked(true)
                .postComment(post1Comment1)
                .build();
        member2LikedComment1 = LikedPostComment.builder()
                .id(2L)
                .isLiked(false)
                .postComment(post1Comment1)
                .build();
    }

    private static void initMemberScrap() {
        member1Scrap2 = MemberScrap.builder()
                .id(1L)
                .post(post2)
                .member(member1)
                .build();
        member2Scrap1 = MemberScrap.builder()
                .id(2L)
                .post(post1)
                .member(member2)
                .build();
    }
}
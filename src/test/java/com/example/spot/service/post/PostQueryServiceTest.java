package com.example.spot.service.post;

import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.*;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.enums.PostStatus;
import com.example.spot.domain.mapping.MemberScrap;
import com.example.spot.repository.*;
import com.example.spot.web.dto.post.*;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private PostReportRepository postReportRepository;

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
    private static Page<Post> postPage;


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
        when(likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(1L)).thenReturn(1L);
        when(likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(2L)).thenReturn(0L);


        // MemberScrap
        when(memberScrapRepository.countByPostId(1L)).thenReturn(1L);
        when(memberScrapRepository.countByPostId(2L)).thenReturn(1L);
        when(memberScrapRepository.findByMemberIdAndPostId(1L, 2L))
                .thenReturn(Optional.of(member1Scrap2));
        when(memberScrapRepository.findByMemberIdAndPostId(2L, 1L))
                .thenReturn(Optional.of(member2Scrap1));
        when(memberScrapRepository.existsByMemberIdAndPostId(1L, 2L)).thenReturn(true);
        when(memberScrapRepository.existsByMemberIdAndPostId(2L, 1L)).thenReturn(true);
    }

/*-------------------------------------------------------- 게시글 단건 조회 ------------------------------------------------------------------------*/

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
    @DisplayName("게시글 단건 조회 - 존재하지 않는 게시글인 경우 (실패)")
    void getPostById_NotExistedPost_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;

        getAuthentication(memberId);

        when(likedPostQueryService.existsByMemberIdAndPostId(postId)).thenReturn(false);

        // when & then
        assertThrows(PostHandler.class, () -> postQueryService.getPostById(postId, false));
    }

    @Test
    @DisplayName("게시글 단건 조회 - 신고된 게시글인 경우 (실패)")
    void getPostById_ReportedPost_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 2L;

        getAuthentication(memberId);

        when(postReportRepository.existsByPostIdAndPostStatus(postId, PostStatus.삭제)).thenReturn(true);
        when(likedPostQueryService.existsByMemberIdAndPostId(postId)).thenReturn(true);

        // when & then
        assertThrows(PostHandler.class, () -> postQueryService.getPostById(postId, false));
    }

/*-------------------------------------------------------- 게시글 페이징 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 페이징 조회 - 전체 게시글 조회 (성공)")
    void getPagingPosts_All_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(post1, post2);
        postPage = new PageImpl<>(posts, pageable, 2);

        when(postRepository.findByPostReportListIsEmpty(pageable)).thenReturn(postPage);

        // when
        PostPagingResponse result = postQueryService.getPagingPosts("ALL", pageable);

        // then
        assertNotNull(result);
        assertThat(result.getPostType()).isEqualTo("ALL");
        assertThat(result.getPostResponses().size()).isEqualTo(2);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글 페이징 조회 - 타입별 게시글 조회 (성공)")
    void getPagingPosts_Type_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(post2);
        postPage = new PageImpl<>(posts, pageable, 1);

        when(postRepository.findByBoardAndPostReportListIsEmpty(Board.INFORMATION_SHARING, pageable)).thenReturn(postPage);

        // when
        PostPagingResponse result = postQueryService.getPagingPosts("INFORMATION_SHARING", pageable);

        // then
        assertNotNull(result);
        assertThat(result.getPostType()).isEqualTo("INFORMATION_SHARING");
        assertThat(result.getPostResponses().size()).isEqualTo(1);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 페이징 조회 - 존재하지 않는 게시판인 경우 (실패)")
    void getPagingPosts_InvalidType_Fail() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        pageable = PageRequest.of(0, 10);
        List<Post> posts = List.of(post2);
        postPage = new PageImpl<>(posts, pageable, 1);

        // when & then
        assertThrows(PostHandler.class, () -> postQueryService.getPagingPosts("INVALID_BOARD", pageable));

    }

/*-------------------------------------------------------- 인기 게시글 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("인기 게시글 조회 - 실시간 인기 게시글 조회 (성공)")
    void getPostBest_Realtime_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        when(postRepository.findTopByRealTimeScore())
                .thenReturn(List.of(post2));

        // when
        PostBest5Response result = postQueryService.getPostBest("REAL_TIME");

        // then
        assertNotNull(result);
        assertThat(result.getSortType()).isEqualTo("REAL_TIME");
        assertThat(result.getPostBest5Responses().size()).isEqualTo(1);
        assertThat(result.getPostBest5Responses().get(0).getPostId()).isEqualTo(post2.getId());
    }

    @Test
    @DisplayName("인기 게시글 조회 - 추천 게시글 조회 (성공)")
    void getPostBest_Recommend_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        when(postRepository.findTopByOrderByLikeNumDesc())
                .thenReturn(List.of(post2, post1));

        // when
        PostBest5Response result = postQueryService.getPostBest("RECOMMEND");

        // then
        assertNotNull(result);
        assertThat(result.getSortType()).isEqualTo("RECOMMEND");
        assertThat(result.getPostBest5Responses().size()).isEqualTo(2);
        assertThat(result.getPostBest5Responses().get(0).getPostId()).isEqualTo(post2.getId());
        assertThat(result.getPostBest5Responses().get(1).getPostId()).isEqualTo(post1.getId());
    }

    @Test
    @DisplayName("인기 게시글 조회 - 댓글순 게시글 조회 (성공)")
    void getPostBest_Comment_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        when(postRepository.findTopByOrderByCommentCountDesc())
                .thenReturn(List.of(post1, post2));

        // when
        PostBest5Response result = postQueryService.getPostBest("COMMENT");

        // then
        assertNotNull(result);
        assertThat(result.getSortType()).isEqualTo("COMMENT");
        assertThat(result.getPostBest5Responses().size()).isEqualTo(2);
        assertThat(result.getPostBest5Responses().get(0).getPostId()).isEqualTo(post1.getId());
        assertThat(result.getPostBest5Responses().get(1).getPostId()).isEqualTo(post2.getId());
    }

    @Test
    @DisplayName("인기 게시글 조회 - 존재하지 않는 필터인 경우 (실패)")
    void getPostBest_InvalidFilter_Fail() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postQueryService.getPostBest("INVALID"));

    }

/*-------------------------------------------------------- 대표 게시글 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("대표 게시글 조회 - (성공)")
    void getRepresentativePosts_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        when(postRepository.findRepresentativePosts()).thenReturn(List.of(post1, post2));

        // when
        PostRepresentativeResponse result = postQueryService.getRepresentativePosts();

        // then
        assertNotNull(result);
        assertThat(result.getResponses().size()).isEqualTo(2);
        assertThat(result.getResponses().get(0).getPostType()).isEqualTo("SPOT_ANNOUNCEMENT");
        assertThat(result.getResponses().get(1).getPostType()).isEqualTo("INFORMATION_SHARING");
    }

/*-------------------------------------------------------- 최신 공지 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("최신 공지 조회 - (성공)")
    void getPostAnnouncements_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        when(postRepository.findAnnouncementPosts()).thenReturn(List.of(post1));

        // when
        PostAnnouncementResponse result = postQueryService.getPostAnnouncements();

        // then
        assertNotNull(result);
        assertThat(result.getResponses().size()).isEqualTo(1);
        assertThat(result.getResponses().get(0).getPostId()).isEqualTo(post1.getId());
    }

/*-------------------------------------------------------- 댓글 목록 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("댓글 목록 조회 - (성공)")
    void getCommentsByPostId_Success() {

        // given
        Long memberId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        // when
        CommentResponse result = postQueryService.getCommentsByPostId(postId);

        // then
        assertNotNull(result);
        assertThat(result.getComments().size()).isEqualTo(2);
        assertThat(result.getComments().get(0).getCommentId()).isEqualTo(1L);
        assertThat(result.getComments().get(1).getCommentId()).isEqualTo(2L);
        assertThat(result.getComments().get(1).getParentCommentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("댓글 목록 조회 - 게시글이 존재하지 않는 경우 (실패)")
    void getCommentsByPostId_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;

        getAuthentication(memberId);

        // when
        assertThrows(PostHandler.class, () -> postQueryService.getCommentsByPostId(postId));
    }

/*-------------------------------------------------------- 스크랩 게시글 페이징 조회 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스크랩 게시글 페이징 조회 - 전체 게시글 조회 (성공)")
    void getScrapPagingPost_All_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        pageable = PageRequest.of(0, 10);
        List<MemberScrap> memberScraps = List.of(member1Scrap2);
        memberScrapPage = new PageImpl<>(memberScraps, pageable, 1);

        when(memberScrapRepository.findByMemberId(1L, pageable)).thenReturn(memberScrapPage);

        // when
        PostPagingResponse result = postQueryService.getScrapPagingPost("ALL", pageable);

        // then
        assertNotNull(result);
        assertThat(result.getPostType()).isEqualTo("ALL");
        assertThat(result.getPostResponses().size()).isEqualTo(1);
        assertThat(result.getPostResponses().get(0).getPostId()).isEqualTo(post2.getId());
    }

    @Test
    @DisplayName("스크랩 게시글 페이징 조회 - 타입별 게시글 조회 (성공)")
    void getScrapPagingPost_Type_Success() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        pageable = PageRequest.of(0, 10);
        List<MemberScrap> memberScraps = List.of(member1Scrap2);
        memberScrapPage = new PageImpl<>(memberScraps, pageable, 1);

        when(memberScrapRepository.findByMemberIdAndPost_Board(1L, Board.INFORMATION_SHARING, pageable))
                .thenReturn(memberScrapPage);

        // when
        PostPagingResponse result = postQueryService.getScrapPagingPost("INFORMATION_SHARING", pageable);

        // then
        assertNotNull(result);
        assertThat(result.getPostType()).isEqualTo("INFORMATION_SHARING");
        assertThat(result.getPostResponses().size()).isEqualTo(1);
        assertThat(result.getPostResponses().get(0).getPostId()).isEqualTo(post2.getId());
    }

    @Test
    @DisplayName("스크랩 게시글 페이징 조회 - 타입이 존재하지 않는 경우 (실패)")
    void getScrapPagingPost_InvalidType_Fail() {

        // given
        Long memberId = 1L;

        getAuthentication(memberId);

        pageable = PageRequest.of(0, 10);
        List<MemberScrap> memberScraps = List.of(member1Scrap2);
        memberScrapPage = new PageImpl<>(memberScraps, pageable, 1);

        // when & then
        assertThrows(PostHandler.class, () -> postQueryService.getScrapPagingPost("INVALID", pageable));
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
                .board(Board.SPOT_ANNOUNCEMENT)
                .member(member1)
                .build();
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));
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
        post2.setCreatedAt(LocalDateTime.now());
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
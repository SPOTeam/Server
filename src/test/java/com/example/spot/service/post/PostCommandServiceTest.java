package com.example.spot.service.post;

import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.*;
import com.example.spot.domain.enums.Board;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostCommandServiceTest {

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
    private PostCommandServiceImpl postCommandService;

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
        when(memberScrapRepository.findByMemberIdAndPostId(1L, 2L))
                .thenReturn(Optional.of(member1Scrap2));
        when(memberScrapRepository.findByMemberIdAndPostId(2L, 1L))
                .thenReturn(Optional.of(member2Scrap1));
        when(memberScrapRepository.existsByMemberIdAndPostId(1L, 2L)).thenReturn(true);
        when(memberScrapRepository.existsByMemberIdAndPostId(2L, 1L)).thenReturn(true);
    }

/*-------------------------------------------------------- 게시글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 작성 - 일반 게시글 (성공)")
    void createPost_Common_Success() {

        // given
        Long memberId = 2L;
        getAuthentication(memberId);

        PostCreateRequest postCreateRequest = PostCreateRequest.builder()
                .title("게시글2")
                .content(null)
                .type(Board.INFORMATION_SHARING)
                .anonymous(false)
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(post2);

        // when
        PostCreateResponse result = postCommandService.createPost(memberId, postCreateRequest);

        // then
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getType()).isEqualTo(Board.INFORMATION_SHARING);
    }

    @Test
    @DisplayName("게시글 작성 - 공지 게시글 (성공)")
    void createPost_Announcement_Success() {

        // given
        Long memberId = 1L;
        getAuthentication(memberId);

        PostCreateRequest postCreateRequest = PostCreateRequest.builder()
                .title("게시글1")
                .content(null)
                .type(Board.SPOT_ANNOUNCEMENT)
                .anonymous(false)
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(post1);

        // when
        PostCreateResponse result = postCommandService.createPost(memberId, postCreateRequest);

        // then
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(Board.SPOT_ANNOUNCEMENT);
    }

    @Test
    @DisplayName("게시글 작성 - 일반 회원이 공지를 작성하는 경우 (실패)")
    void createPost_Announcement_Fail() {

        // given
        Long memberId = 2L;
        getAuthentication(memberId);

        PostCreateRequest postCreateRequest = PostCreateRequest.builder()
                .title("게시글1")
                .content(null)
                .type(Board.SPOT_ANNOUNCEMENT)
                .anonymous(false)
                .build();

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.createPost(memberId, postCreateRequest));
    }

/*-------------------------------------------------------- 게시글 수정 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 수정 - 공지 게시글 (성공)")
    void updatePost_Announcement_Success() {

        // given
        Long memberId = 1L;
        Long postId = 1L;
        getAuthentication(memberId);

        PostUpdateRequest postUpdateRequest = PostUpdateRequest.builder()
                .title("수정된 게시글1")
                .content("내용")
                .isAnonymous(true)
                .type("SPOT_ANNOUNCEMENT")
                .build();

        // when
        PostCreateResponse result = postCommandService.updatePost(memberId, postId, postUpdateRequest);

        // then
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(Board.SPOT_ANNOUNCEMENT);
    }

    @Test
    @DisplayName("게시글 수정 - 일반 게시글 (성공)")
    void updatePost_Comment_Success() {

        // given
        Long memberId = 2L;
        Long postId = 2L;
        getAuthentication(memberId);

        PostUpdateRequest postUpdateRequest = PostUpdateRequest.builder()
                .title("수정된 게시글2")
                .content("내용")
                .isAnonymous(true)
                .type("JOB_TALK")
                .build();

        // when
        PostCreateResponse result = postCommandService.updatePost(memberId, postId, postUpdateRequest);

        // then
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getType()).isEqualTo(Board.JOB_TALK);
    }

    @Test
    @DisplayName("게시글 수정 - 일반 회원이 공지를 작성하는 경우 (실패)")
    void updatePost_Announcement_Fail() {

        // given
        Long memberId = 2L;
        Long postId = 2L;
        getAuthentication(memberId);

        PostUpdateRequest postUpdateRequest = PostUpdateRequest.builder()
                .title("수정된 게시글2")
                .content("내용")
                .isAnonymous(true)
                .type("SPOT_ANNOUNCEMENT")
                .build();

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.updatePost(memberId, postId, postUpdateRequest));
    }

    @Test
    @DisplayName("게시글 수정 - 게시글 작성자가 아닌 경우 (실패)")
    void updatePost_NotWriter_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        PostUpdateRequest postUpdateRequest = PostUpdateRequest.builder()
                .title("수정된 게시글2")
                .content("내용")
                .isAnonymous(true)
                .type("JOB_TALK")
                .build();

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.updatePost(memberId, postId, postUpdateRequest));
    }

    @Test
    @DisplayName("게시글 수정 - 게시글이 존재하지 않는 경우 (실패)")
    void updatePost_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;
        getAuthentication(memberId);

        PostUpdateRequest postUpdateRequest = PostUpdateRequest.builder()
                .title("수정된 게시글3")
                .content("내용")
                .isAnonymous(true)
                .type("JOB_TALK")
                .build();

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.updatePost(memberId, postId, postUpdateRequest));
    }

/*-------------------------------------------------------- 게시글 삭제 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 삭제 - (성공)")
    void deletePost_Success() {

        // given
        Long memberId = 1L;
        Long postId = 1L;
        getAuthentication(memberId);

        // when & then
        postCommandService.deletePost(memberId, postId);
    }

    @Test
    @DisplayName("게시글 삭제 - 게시글 작성자가 아닌 경우 (실패)")
    void deletePost_NotWriter_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.deletePost(memberId, postId));
    }

    @Test
    @DisplayName("게시글 삭제 - 게시글이 존재하지 않는 경우 (실패)")
    void deletePost_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.deletePost(memberId, postId));
    }

/*-------------------------------------------------------- 게시글 좋아요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 좋아요 - (성공)")
    void likePost_Success() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        when(likedPostRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.empty());
        when(likedPostRepository.saveAndFlush(any(LikedPost.class)))
                .thenReturn(member1LikedPost2);

        // when
        PostLikeResponse result = postCommandService.likePost(postId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(2L);
        assertThat(result.getLikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 좋아요 - 게시글이 존재하지 않는 경우 (실패)")
    void likePost_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.likePost(postId, memberId));
    }

    @Test
    @DisplayName("게시글 좋아요 - 이미 좋아요 한 경우 (실패)")
    void likePost_AlreadyLiked_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        when(likedPostRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.of(member1LikedPost2));

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.likePost(postId, memberId));
    }

/*-------------------------------------------------------- 게시글 좋아요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 좋아요 취소 - (성공)")
    void cancelPostLike_Success() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        when(likedPostRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.of(member1LikedPost2));
        when(likedPostQueryService.countByPostId(postId)).thenReturn(0L);

        // when
        PostLikeResponse result = postCommandService.cancelPostLike(postId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(2L);
        assertThat(result.getLikeCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 게시글이 존재하지 않는 경우 (실패)")
    void cancelPostLike_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.cancelPostLike(postId, memberId));
    }

    @Test
    @DisplayName("게시글 좋아요 취소 - 좋아요 한 게시글이 아닌 경우 (실패)")
    void cancelPostLike_NotLiked_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 1L;
        getAuthentication(memberId);

        when(likedPostRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.cancelPostLike(postId, memberId));
    }

/*-------------------------------------------------------- 댓글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("댓글 작성 - 상위 댓글 (성공)")
    void createComment_Parent_Success() {

        // given
        Long memberId = 1L;
        Long postId = 1L;
        getAuthentication(memberId);

        CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
                .content("댓글1")
                .anonymous(true)
                .parentCommentId(null)
                .build();

        when(postCommentRepository.saveAndFlush(any(PostComment.class))).thenReturn(post1Comment1);

        // when
        CommentCreateResponse result = postCommandService.createComment(postId, memberId, commentCreateRequest);

        // then
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("댓글1");
        assertThat(result.getWriter()).isEqualTo("익명");
    }

    @Test
    @DisplayName("댓글 작성 - 하위 댓글 (성공)")
    void createComment_Child_Success() {

        // given
        Long memberId = 2L;
        Long postId = 1L;
        getAuthentication(memberId);

        CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
                .content("댓글2")
                .anonymous(false)
                .parentCommentId(1L)
                .build();

        when(postCommentRepository.saveAndFlush(any(PostComment.class))).thenReturn(post1Comment2);

        // when
        CommentCreateResponse result = postCommandService.createComment(postId, memberId, commentCreateRequest);

        // then
        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getContent()).isEqualTo("댓글2");
        assertThat(result.getWriter()).isEqualTo("회원2");
    }

    @Test
    @DisplayName("댓글 작성 - 게시글이 존재하지 않는 경우 (실패)")
    void createComment_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;
        getAuthentication(memberId);

        CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
                .content("댓글3")
                .anonymous(false)
                .parentCommentId(null)
                .build();

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.createComment(postId, memberId, commentCreateRequest));
    }

    @Test
    @DisplayName("댓글 작성 - 상위 댓글이 존재하지 않는 경우 (실패)")
    void createComment_InvalidParent_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        CommentCreateRequest commentCreateRequest = CommentCreateRequest.builder()
                .content("댓글3")
                .anonymous(false)
                .parentCommentId(3L)
                .build();

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.createComment(postId, memberId, commentCreateRequest));
    }

/*-------------------------------------------------------- 댓글 좋아요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("댓글 좋아요 - (성공)")
    void likeComment_Success() {

        // given
        Long memberId = 1L;
        Long commentId = 1L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId))
                .thenReturn(Optional.empty());
        when(likedPostCommentRepository.saveAndFlush(any(LikedPostComment.class)))
                .thenReturn(member1LikedComment1);
        when(likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId))
                .thenReturn(1L);

        // when
        CommentLikeResponse result = postCommandService.likeComment(commentId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getDisLikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("댓글 좋아요 - 댓글이 존재하지 않는 경우 (실패)")
    void likeComment_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long commentId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.likeComment(commentId, memberId));
    }

    @Test
    @DisplayName("댓글 좋아요 - 이미 좋아요 한 경우 (실패)")
    void likeComment_AlreadyLiked_Fail() {

        // given
        Long memberId = 1L;
        Long commentId = 3L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId))
                .thenReturn(Optional.of(member1LikedComment1));

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.likeComment(commentId, memberId));
    }

/*-------------------------------------------------------- 댓글 좋아요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("댓글 좋아요 취소 - (성공)")
    void cancelCommentLike_Success() {

        // given
        Long memberId = 1L;
        Long commentId = 1L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId))
                .thenReturn(Optional.of(member1LikedComment1));
        when(likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId))
                .thenReturn(0L);

        // when
        CommentLikeResponse result = postCommandService.cancelCommentLike(commentId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getLikeCount()).isEqualTo(0L);
        assertThat(result.getDisLikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("댓글 좋아요 취소 - 댓글이 존재하지 않는 경우 (실패)")
    void cancelCommentLike_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long commentId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () ->postCommandService.cancelCommentLike(commentId, memberId));
    }

    @Test
    @DisplayName("댓글 좋아요 취소 - 좋아요 한 댓글이 아닌 경우 (실패)")
    void cancelCommentLike_NotLiked_Fail() {

        // given
        Long memberId = 1L;
        Long commentId = 2L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedTrue(memberId, commentId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(PostHandler.class, () ->postCommandService.cancelCommentLike(commentId, memberId));
    }

/*-------------------------------------------------------- 댓글 싫어요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("댓글 싫어요 - (성공)")
    void dislikeComment_Success() {

        // given
        Long memberId = 1L;
        Long commentId = 1L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId))
                .thenReturn(Optional.empty());
        when(likedPostCommentRepository.saveAndFlush(any(LikedPostComment.class)))
                .thenReturn(member1LikedComment1);
        when(likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId))
                .thenReturn(1L);
        when(likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId))
                .thenReturn(1L);

        // when
        CommentLikeResponse result = postCommandService.dislikeComment(commentId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getDisLikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("댓글 싫어요 - 댓글이 존재하지 않는 경우 (실패)")
    void dislikeComment_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long commentId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.dislikeComment(commentId, memberId));

    }

    @Test
    @DisplayName("댓글 싫어요 - 이미 싫어요 한 경우 (실패)")
    void dislikeComment_AlreadyDisliked_Fail() {

        // given
        Long memberId = 2L;
        Long commentId = 1L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId))
                .thenReturn(Optional.of(member2LikedComment1));

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.dislikeComment(commentId, memberId));
    }

/*-------------------------------------------------------- 댓글 싫어요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("댓글 싫어요 취소 - (성공)")
    void cancelCommentDislike_Success() {

        // given
        Long memberId = 2L;
        Long commentId = 1L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId))
                .thenReturn(Optional.of(member2LikedComment1));
        when(likedPostCommentQueryService.countByPostCommentIdAndIsLikedTrue(commentId))
                .thenReturn(1L);
        when(likedPostCommentRepository.countByPostCommentIdAndIsLikedFalse(commentId))
                .thenReturn(0L);

        // when
        CommentLikeResponse result = postCommandService.cancelCommentDislike(commentId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getDisLikeCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("댓글 싫어요 취소 - 댓글이 존재하지 않는 경우 (실패)")
    void cancelCommentDislike_NotExisted_Fail() {

        // given
        Long memberId = 2L;
        Long commentId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.cancelCommentDislike(commentId, memberId));
    }

    @Test
    @DisplayName("댓글 싫어요 취소 - 싫어요 한 댓글이 아닌 경우 (실패)")
    void cancelCommentDislike_NotDisliked_Fail() {

        // given
        Long memberId = 1L;
        Long commentId = 1L;
        getAuthentication(memberId);

        when(likedPostCommentRepository.findByMemberIdAndPostCommentIdAndIsLikedFalse(memberId, commentId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.cancelCommentDislike(commentId, memberId));
    }

/*-------------------------------------------------------- 게시글 스크랩 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 스크랩 - (성공)")
    void scrapPost_Success() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        when(memberScrapRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.empty());
        when(memberScrapRepository.saveAndFlush(any(MemberScrap.class)))
                .thenReturn(member1Scrap2);

        // when
        ScrapPostResponse result = postCommandService.scrapPost(postId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(2L);
        assertThat(result.getScrapCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 스크랩 - 게시글이 존재하지 않는 경우 (실패)")
    void scrapPost_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;
        getAuthentication(memberId);

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.scrapPost(postId, memberId));
    }

    @Test
    @DisplayName("게시글 스크랩 - 이미 스크랩 한 경우 (실패)")
    void scrapPost_AlreadyScraped_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 2L;
        getAuthentication(memberId);

        when(memberScrapRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.of(member1Scrap2));

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.scrapPost(postId, memberId));
    }

/*-------------------------------------------------------- 게시글 스크랩 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("게시글 스크랩 취소 - (성공)")
    void cancelPostScrap_Success() {

        // given
        Long memberId = 1L;
        Long postId = 2L;

        when(memberScrapRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.of(member1Scrap2));
        when(memberScrapRepository.countByPostId(postId)).thenReturn(1L);

        // when
        ScrapPostResponse result = postCommandService.cancelPostScrap(postId, memberId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(2L);
        assertThat(result.getScrapCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 스크랩 취소 - 게시글이 존재하지 않는 경우 (실패)")
    void cancelPostScrap_NotExisted_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 3L;

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.cancelPostScrap(postId, memberId));
    }

    @Test
    @DisplayName("게시글 스크랩 취소 - 스크랩한 게시글이 아닌 경우 (실패)")
    void cancelPostScrap_NotScraped_Fail() {

        // given
        Long memberId = 1L;
        Long postId = 1L;

        when(memberScrapRepository.findByMemberIdAndPostId(memberId, postId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(PostHandler.class, () -> postCommandService.cancelPostScrap(postId, memberId));
    }

/*-------------------------------------------------------- 게시글 스크랩 다중 취소 ------------------------------------------------------------------------*/

    @Test
    void cancelPostScraps() {
    }

/*-------------------------------------------------------- 게시글 신고 ------------------------------------------------------------------------*/

    @Test
    void reportPost() {
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
                .isAdmin(true)
                .postList(new ArrayList<>())
                .likedPostList(new ArrayList<>())
                .memberScrapList(new ArrayList<>())
                .postCommentList(new ArrayList<>())
                .likedCommentList(new ArrayList<>())
                .build();
        member2 = Member.builder()
                .id(2L)
                .nickname("회원2")
                .isAdmin(false)
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
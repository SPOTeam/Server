package com.example.spot.service.studypost;

import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Notification;
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
import com.example.spot.web.dto.memberstudy.request.StudyPostCommentRequestDTO;
import com.example.spot.web.dto.memberstudy.request.StudyPostRequestDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    @Mock
    private StudyLikedCommentRepository studyLikedCommentRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private StudyPostCommandServiceImpl studyPostCommandService;

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
        when(studyPostCommentRepository.findById(1L)).thenReturn(Optional.of(studyPost1Comment1));
        when(studyPostCommentRepository.findById(2L)).thenReturn(Optional.of(studyPost1Comment2));

    }

/*-------------------------------------------------------- 게시글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 작성 - 공지 게시글 (성공)")
    void createPost_Announcement_Success() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;

        StudyPostRequestDTO.PostDTO postPreviewDTO = StudyPostRequestDTO.PostDTO.builder()
                .isAnnouncement(true)
                .theme(Theme.INFO_SHARING)
                .title("공지")
                .content("내용")
                .build();

        getAuthentication(memberId);

        when(memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, true))
                .thenReturn(Optional.of(ownerStudy));
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost2);
        when(notificationRepository.save(any(Notification.class))).thenReturn(null);
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(member1Study, ownerStudy));

        // when
        StudyPostResDTO.PostPreviewDTO result = studyPostCommandService.createPost(studyId, postPreviewDTO);

        // then
        assertNotNull(result);
        assertThat(result.getTitle()).isEqualTo("공지");
        verify(studyPostRepository, times(1)).save(any(StudyPost.class));
    }

    @Test
    @DisplayName("스터디 게시글 작성 - 일반 게시글 (성공)")
    void createPost_Common_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;

        StudyPostRequestDTO.PostDTO postPreviewDTO = StudyPostRequestDTO.PostDTO.builder()
                .isAnnouncement(false)
                .theme(Theme.FREE_TALK)
                .title("잡담")
                .content("내용")
                .build();

        getAuthentication(memberId);

        when(memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, true))
                .thenReturn(Optional.of(member1Study));
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(notificationRepository.save(any(Notification.class))).thenReturn(null);
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(member1Study, ownerStudy));

        // when
        StudyPostResDTO.PostPreviewDTO result = studyPostCommandService.createPost(studyId, postPreviewDTO);

        // then
        assertNotNull(result);
        assertThat(result.getTitle()).isEqualTo("잡담");
        verify(studyPostRepository, times(1)).save(any(StudyPost.class));
    }

    @Test
    @DisplayName("스터디 게시글 작성 - 스터디 회원이 아닌 경우 (실패)")
    void createPost_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;

        StudyPostRequestDTO.PostDTO postPreviewDTO = StudyPostRequestDTO.PostDTO.builder()
                .isAnnouncement(true)
                .theme(Theme.INFO_SHARING)
                .title("공지")
                .content("내용")
                .build();

        getAuthentication(memberId);

        when(memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, true))
                .thenReturn(Optional.of(ownerStudy));
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost2);
        when(notificationRepository.save(any(Notification.class))).thenReturn(null);
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(member1Study, ownerStudy));

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.createPost(studyId, postPreviewDTO));
    }

    @Test
    @DisplayName("스터디 게시글 작성 - 스터디장이 아닌 회원이 공지 게시글을 작성하는 경우 (실패)")
    void createPost_MemberAnnounced_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;

        StudyPostRequestDTO.PostDTO postPreviewDTO = StudyPostRequestDTO.PostDTO.builder()
                .isAnnouncement(true)
                .theme(Theme.INFO_SHARING)
                .title("공지")
                .content("내용")
                .build();

        getAuthentication(memberId);

        when(memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, true))
                .thenReturn(Optional.of(member1Study));
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost2);
        when(notificationRepository.save(any(Notification.class))).thenReturn(null);
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(member1Study, ownerStudy));

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.createPost(studyId, postPreviewDTO));
    }

    @Test
    @DisplayName("스터디 게시글 작성 - 제목이 50자를 초과하는 경우 (실패)")
    void createPost_TitleOverflow_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;

        StudyPostRequestDTO.PostDTO postPreviewDTO = StudyPostRequestDTO.PostDTO.builder()
                .isAnnouncement(true)
                .theme(Theme.INFO_SHARING)
                .title("50자가 넘어가는 제목 "
                        + "50자가 넘어가는 제목 "
                        + "50자가 넘어가는 제목 "
                        + "50자가 넘어가는 제목 "
                        + "50자가 넘어가는 제목 ")
                .content("내용")
                .build();

        getAuthentication(memberId);

        when(memberStudyRepository.findByMemberIdAndStudyIdAndIsOwned(memberId, studyId, true))
                .thenReturn(Optional.of(ownerStudy));
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost2);
        when(notificationRepository.save(any(Notification.class))).thenReturn(null);
        when(memberStudyRepository.findAllByStudyIdAndStatus(studyId, ApplicationStatus.APPROVED))
                .thenReturn(List.of(member1Study, ownerStudy));

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.createPost(studyId, postPreviewDTO));
    }


/*-------------------------------------------------------- 게시글 삭제 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 삭제 - (성공)")
    void deletePost_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostRepository.findByIdAndMemberId(postId, memberId))
                .thenReturn(Optional.of(studyPost1));

        // when
        StudyPostResDTO.PostPreviewDTO result = studyPostCommandService.deletePost(studyId, postId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("잡담");
        verify(studyPostRepository, times(1)).delete(any(StudyPost.class));
        verify(studyLikedPostRepository, times(1)).deleteAllByStudyPostId(postId);
    }

    @Test
    @DisplayName("스터디 게시글 삭제 - 이미 삭제된 게시글인 경우 (실패)")
    void deletePost_AlreadyDeleted_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.empty());
        when(studyPostRepository.findByIdAndMemberId(postId, memberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.deletePost(studyId, postId));
    }

    @Test
    @DisplayName("스터디 게시글 삭제 - 작성자 본인이나 스터디장이 아닌 경우 (실패)")
    void deletePost_NotAvailableMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostRepository.findByIdAndMemberId(postId, memberId))
                .thenReturn(Optional.of(studyPost1));

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.deletePost(studyId, postId));
    }

/*-------------------------------------------------------- 게시글 좋아요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 좋아요 - (성공)")
    void likePost_Success() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(Optional.empty());
        when(studyLikedPostRepository.save(any(StudyLikedPost.class))).thenReturn(studyLikedPost);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);

        // when
        StudyPostResDTO.PostLikeNumDTO result = studyPostCommandService.likePost(studyId, postId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("잡담");
        assertThat(result.getLikeNum()).isEqualTo(2);
        verify(studyLikedPostRepository, times(1)).save(any(StudyLikedPost.class));
    }

    @Test
    @DisplayName("스터디 게시글 좋아요 - 스터디 회원이 아닌 경우 (실패)")
    void likePost_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(Optional.empty());
        when(studyLikedPostRepository.save(any(StudyLikedPost.class))).thenReturn(studyLikedPost);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.likePost(studyId, postId));
    }
    @Test
    @DisplayName("스터디 게시글 좋아요 - 이미 좋아요를 누른 경우 (실패)")
    void likePost_AlreadyLiked_Fail() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(Optional.of(studyLikedPost));
        when(studyLikedPostRepository.save(any(StudyLikedPost.class))).thenReturn(studyLikedPost);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.likePost(studyId, postId));
    }


/*-------------------------------------------------------- 게시글 좋아요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 좋아요 취소 - (성공)")
    void cancelPostLike_Success() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(Optional.of(studyLikedPost));
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);

        // when
        StudyPostResDTO.PostLikeNumDTO result = studyPostCommandService.cancelPostLike(studyId, postId);

        // then
        assertNotNull(result);
        assertThat(result.getPostId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("잡담");
        assertThat(result.getLikeNum()).isEqualTo(0);
    }

    @Test
    @DisplayName("스터디 게시글 좋아요 취소 - 스터디 회원이 아닌 경우 (실패)")
    void cancelPostLike_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(Optional.of(studyLikedPost));
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.cancelPostLike(studyId, postId));
    }

    @Test
    @DisplayName("스터디 게시글 좋아요 취소 - 좋아요를 누르지 않은 게시글인 경우 (실패)")
    void cancelPostLike_NotLiked_Fail() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(Optional.empty());
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.cancelPostLike(studyId, postId));
    }


/*-------------------------------------------------------- 댓글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 작성 - 익명 댓글 (성공)")
    void createComment_Anonymous_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        StudyPostCommentRequestDTO.CommentDTO commentDTO = StudyPostCommentRequestDTO.CommentDTO.builder()
                .content("댓글")
                .isAnonymous(true)
                .build();

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment1);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(studyPostCommentRepository.findAllByStudyPostId(postId)).thenReturn(List.of());
        when(studyPostCommentRepository.findAllByMemberIdAndStudyPostId(memberId, postId)).thenReturn(List.of());

        // when
        StudyPostCommentResponseDTO.CommentDTO result = studyPostCommandService.createComment(studyId, postId, commentDTO);

        // then
        assertNotNull(result);
        assertThat(result.getMember().getMemberId()).isEqualTo(1L);
        assertThat(result.getMember().getName()).isEqualTo("익명1");
        assertThat(result.getContent()).isEqualTo("댓글");
    }

    @Test
    @DisplayName("스터디 게시글 댓글 작성 - 실명 댓글 (성공)")
    void createComment_Name_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        StudyPostCommentRequestDTO.CommentDTO commentDTO = StudyPostCommentRequestDTO.CommentDTO.builder()
                .content("댓글")
                .isAnonymous(false)
                .build();

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment1);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(studyPostCommentRepository.findAllByStudyPostId(postId)).thenReturn(List.of());
        when(studyPostCommentRepository.findAllByMemberIdAndStudyPostId(memberId, postId)).thenReturn(List.of());

        // when
        StudyPostCommentResponseDTO.CommentDTO result = studyPostCommandService.createComment(studyId, postId, commentDTO);

        // then
        assertNotNull(result);
        assertThat(result.getMember().getMemberId()).isEqualTo(1L);
        assertThat(result.getMember().getName()).isEqualTo("회원1");
        assertThat(result.getContent()).isEqualTo("댓글");
    }

    @Test
    @DisplayName("스터디 게시글 댓글 작성 - 스터디 회원이 아닌 경우 (실패)")
    void createComment_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        StudyPostCommentRequestDTO.CommentDTO commentDTO = StudyPostCommentRequestDTO.CommentDTO.builder()
                .content("댓글")
                .isAnonymous(false)
                .build();

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment1);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(studyPostCommentRepository.findAllByStudyPostId(postId)).thenReturn(List.of());
        when(studyPostCommentRepository.findAllByMemberIdAndStudyPostId(memberId, postId)).thenReturn(List.of());

        // when
        assertThrows(StudyHandler.class, () -> studyPostCommandService.createComment(studyId, postId, commentDTO));
    }



/*-------------------------------------------------------- 답글 작성 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 답글 작성 - 익명 댓글 (성공)")
    void createReply_Anonymous_Success() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 1L;

        getAuthentication(memberId);

        StudyPostCommentRequestDTO.CommentDTO commentDTO = StudyPostCommentRequestDTO.CommentDTO.builder()
                .content("답글")
                .isAnonymous(true)
                .build();

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment1);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1));
        when(studyPostCommentRepository.findAllByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(List.of());

        // when
        StudyPostCommentResponseDTO.CommentDTO result = studyPostCommandService
                .createReply(studyId, postId, commentId, commentDTO);

        //then
        assertNotNull(result);
        assertThat(result.getMember().getMemberId()).isEqualTo(3L);
        assertThat(result.getMember().getName()).isEqualTo("익명2");
        assertThat(result.getContent()).isEqualTo("답글");
    }

    @Test
    @DisplayName("스터디 게시글 답글 작성 - 실명 댓글 (성공)")
    void createReply_Name_Success() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 1L;

        getAuthentication(memberId);

        StudyPostCommentRequestDTO.CommentDTO commentDTO = StudyPostCommentRequestDTO.CommentDTO.builder()
                .content("답글")
                .isAnonymous(false)
                .build();

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment1);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1));
        when(studyPostCommentRepository.findAllByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(List.of());

        // when
        StudyPostCommentResponseDTO.CommentDTO result = studyPostCommandService
                .createReply(studyId, postId, commentId, commentDTO);

        //then
        assertNotNull(result);
        assertThat(result.getMember().getMemberId()).isEqualTo(3L);
        assertThat(result.getMember().getName()).isEqualTo("회원3");
        assertThat(result.getContent()).isEqualTo("답글");
    }

    @Test
    @DisplayName("스터디 게시글 답글 작성 - 스터디 회원이 아닌 경우 (실패)")
    void createReply_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 1L;

        getAuthentication(memberId);

        StudyPostCommentRequestDTO.CommentDTO commentDTO = StudyPostCommentRequestDTO.CommentDTO.builder()
                .content("답글")
                .isAnonymous(false)
                .build();

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment1);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1));
        when(studyPostCommentRepository.findAllByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(List.of());

        // when
        assertThrows(StudyHandler.class, () -> studyPostCommandService.createReply(studyId, postId, commentId, commentDTO));
    }

    @Test
    @DisplayName("스터디 게시글 답글 작성 - 상위 댓글이 존재하지 않는 경우 (실패)")
    void createReply_ParentCommentNotExist_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;

        getAuthentication(memberId);

        StudyPostCommentRequestDTO.CommentDTO commentDTO = StudyPostCommentRequestDTO.CommentDTO.builder()
                .content("답글")
                .isAnonymous(false)
                .build();

        when(studyPostRepository.findByIdAndStudyId(postId, studyId))
                .thenReturn(Optional.of(studyPost1));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment1);
        when(studyPostRepository.save(any(StudyPost.class))).thenReturn(studyPost1);
        when(studyPostCommentRepository.findAllByStudyPostId(postId))
                .thenReturn(List.of(studyPost1Comment1, studyPost1Comment2));
        when(studyPostCommentRepository.findAllByMemberIdAndStudyPostId(memberId, postId))
                .thenReturn(List.of(studyPost1Comment1));

        // when
        assertThrows(StudyHandler.class, () -> studyPostCommandService.createReply(studyId, postId, null, commentDTO));
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

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 1L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, true))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, false))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.save(any(StudyLikedComment.class))).thenReturn(studyLikedComment);

        // when
        StudyPostCommentResponseDTO.CommentPreviewDTO result = studyPostCommandService.likeComment(studyId, postId, commentId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getDislikeCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 - 스터디 회원이 아닌 경우 (실패)")
    void likeComment_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 1L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, true))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, false))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.save(any(StudyLikedComment.class))).thenReturn(studyLikedComment);

        // when
        assertThrows(StudyHandler.class, () -> studyPostCommandService.likeComment(studyId, postId, commentId));
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 - 이미 좋아요를 누른 경우 (실패)")
    void likeComment_AlreadyLiked_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, true))
                .thenReturn(Optional.of(studyLikedComment));
        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, false))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.save(any(StudyLikedComment.class))).thenReturn(studyLikedComment);

        // when
        assertThrows(StudyHandler.class, () -> studyPostCommandService.likeComment(studyId, postId, commentId));
    }


/*-------------------------------------------------------- 댓글 싫어요 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 - (성공)")
    void dislikeComment_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 1L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, true))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, false))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.save(any(StudyLikedComment.class))).thenReturn(studyLikedComment);

        // when
        StudyPostCommentResponseDTO.CommentPreviewDTO result = studyPostCommandService.dislikeComment(studyId, postId, commentId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(1L);
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getDislikeCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 - 스터디 회원인 아닌 경우(실패)")
    void dislikeComment_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 1L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, true))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, false))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.save(any(StudyLikedComment.class))).thenReturn(studyLikedComment);

        // when
        assertThrows(StudyHandler.class, () -> studyPostCommandService.dislikeComment(studyId, postId, commentId));

    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 - 이미 싫어요를 누른 경우(실패)")
    void dislikeComment_AlreadyDisliked_Fail() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, true))
                .thenReturn(Optional.empty());
        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, false))
                .thenReturn(Optional.of(studyLikedComment));
        when(studyLikedCommentRepository.save(any(StudyLikedComment.class))).thenReturn(studyLikedComment);

        // when
        assertThrows(StudyHandler.class, () -> studyPostCommandService.dislikeComment(studyId, postId, commentId));
    }

/*-------------------------------------------------------- 댓글 좋아요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 취소 - (성공)")
    void cancelCommentLike_Success() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, studyPost1Comment2.getId(), true))
                .thenReturn(Optional.of(studyLikedComment));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment2);

        // when
        StudyPostCommentResponseDTO.CommentPreviewDTO result = studyPostCommandService
                .cancelCommentLike(studyId, postId, commentId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(2L);
        assertThat(result.getLikeCount()).isEqualTo(0L);
        assertThat(result.getDislikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 취소 - 스터디 회원이 아닌 경우 (실패)")
    void cancelCommentLike_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, studyPost1Comment2.getId(), true))
                .thenReturn(Optional.of(studyLikedComment));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment2);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.cancelCommentLike(studyId, postId, commentId));
    }

    @Test
    @DisplayName("스터디 게시글 댓글 좋아요 취소 - 좋아요를 누른 댓글이 아닌 경우 (실패)")
    void cancelCommentLike_NotLiked_Fail() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, studyPost1Comment2.getId(), true))
                .thenReturn(Optional.empty());
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment2);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.cancelCommentLike(studyId, postId, commentId));
    }

/*-------------------------------------------------------- 댓글 싫어요 취소 ------------------------------------------------------------------------*/

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 취소 - (성공)")
    void cancelCommentDislike() {

        // given
        Long memberId = 3L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, studyPost1Comment2.getId(), false))
                .thenReturn(Optional.of(studyDislikedComment));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment2);

        // when
        StudyPostCommentResponseDTO.CommentPreviewDTO result = studyPostCommandService
                .cancelCommentDislike(studyId, postId, commentId);

        // then
        assertNotNull(result);
        assertThat(result.getCommentId()).isEqualTo(2L);
        assertThat(result.getLikeCount()).isEqualTo(1L);
        assertThat(result.getDislikeCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 취소 - 스터디 회원이 아닌 경우 (실패)")
    void cancelCommentDislike_NotStudyMember_Fail() {

        // given
        Long memberId = 2L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, studyPost1Comment2.getId(), false))
                .thenReturn(Optional.of(studyDislikedComment));
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment2);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.cancelCommentDislike(studyId, postId, commentId));
    }

    @Test
    @DisplayName("스터디 게시글 댓글 싫어요 취소 - 싫어요를 누른 댓글이 아닌 경우 (실패)")
    void cancelCommentDislike_NotDisliked_Fail() {

        // given
        Long memberId = 1L;
        Long studyId = 1L;
        Long postId = 1L;
        Long commentId = 2L;

        getAuthentication(memberId);

        when(studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, studyPost1Comment2.getId(), false))
                .thenReturn(Optional.empty());
        when(studyPostCommentRepository.save(any(StudyPostComment.class))).thenReturn(studyPost1Comment2);

        // when & then
        assertThrows(StudyHandler.class, () -> studyPostCommandService.cancelCommentDislike(studyId, postId, commentId));
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
                .name("회원1")
                .studyPostList(new ArrayList<>())
                .studyLikedPostList(new ArrayList<>())
                .studyPostCommentList(new ArrayList<>())
                .studyLikedCommentList(new ArrayList<>())
                .build();
        member2 = Member.builder()
                .id(2L)
                .name("회원2")
                .studyPostList(new ArrayList<>())
                .studyLikedPostList(new ArrayList<>())
                .studyPostCommentList(new ArrayList<>())
                .studyLikedCommentList(new ArrayList<>())
                .build();
        owner = Member.builder()
                .id(3L)
                .name("회원3")
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
        owner.addMemberStudy(ownerStudy);
        study.addMemberStudy(ownerStudy);

        member1Study = MemberStudy.builder()
                .id(2L)
                .status(ApplicationStatus.APPROVED)
                .isOwned(false)
                .introduction("Hi")
                .member(member1)
                .study(study)
                .build();
        member1.addMemberStudy(member1Study);
        study.addMemberStudy(member1Study);
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
        owner.addStudyLikedPost(studyLikedPost);
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
                .anonymousNum(1)
                .isDeleted(false)
                .parentComment(null)
                .build();
        studyPost1.addComment(studyPost1Comment1);

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
        studyPost1Comment2.plusLikeCount();
        member1.addStudyLikedComment(studyLikedComment);

        studyDislikedComment = StudyLikedComment.builder()
                .id(2L)
                .isLiked(false)
                .studyPostComment(studyPost1Comment2)
                .member(owner)
                .build();
        studyPost1Comment2.addLikedComment(studyDislikedComment);
        studyPost1Comment2.plusDislikeCount();
        owner.addStudyLikedComment(studyDislikedComment);
    }
}
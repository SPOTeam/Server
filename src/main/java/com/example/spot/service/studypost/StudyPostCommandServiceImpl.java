package com.example.spot.service.studypost;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.StudyLikedComment;
import com.example.spot.domain.mapping.StudyLikedPost;
import com.example.spot.domain.mapping.StudyPostImage;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.domain.study.StudyPostComment;
import com.example.spot.repository.*;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.s3.S3ImageService;
import com.example.spot.web.dto.memberstudy.request.StudyPostCommentRequestDTO;
import com.example.spot.web.dto.memberstudy.request.StudyPostRequestDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostCommentResponseDTO;
import com.example.spot.web.dto.memberstudy.response.StudyPostResDTO;
import com.example.spot.web.dto.util.response.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyPostCommandServiceImpl implements StudyPostCommandService {

    @Value("${cloud.aws.default-image}")
    private String defaultImage;

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;

    private final MemberStudyRepository memberStudyRepository;
    private final StudyPostRepository studyPostRepository;
    private final StudyPostImageRepository studyPostImageRepository;
    private final StudyPostCommentRepository studyPostCommentRepository;
    private final StudyLikedPostRepository studyLikedPostRepository;
    private final StudyLikedCommentRepository studyLikedCommentRepository;
    private final NotificationRepository notificationRepository;

    // S3 Service
    private final S3ImageService s3ImageService;

/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    @Override
    public StudyPostResDTO.PostPreviewDTO createPost(Long studyId, StudyPostRequestDTO.PostDTO postRequestDTO) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 스터디장만 공지 가능
        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
        if (!memberStudy.getIsOwned() && postRequestDTO.getIsAnnouncement()) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_ANNOUNCEMENT_INVALID);
        }

        //=== Feature ===//
        StudyPost studyPost = StudyPost.builder()
                .isAnnouncement(postRequestDTO.getIsAnnouncement())
                .theme(postRequestDTO.getTheme())
                .title(postRequestDTO.getTitle())
                .content(postRequestDTO.getContent())
                .build();

        // 공지면 announcedAt 설정
        if (studyPost.getIsAnnouncement()) {
            studyPost.setAnnouncedAt(LocalDateTime.now());
        }

        member.addStudyPost(studyPost);
        study.addStudyPost(studyPost);
        studyPost = studyPostRepository.save(studyPost);

        if (postRequestDTO.getImages() != null && !postRequestDTO.getImages().isEmpty()) {
            ImageResponse.ImageUploadResponse imageUploadResponse = s3ImageService.uploadImages(postRequestDTO.getImages());
            for (ImageResponse.Images imageDTO : imageUploadResponse.getImageUrls()) {
                String imageUrl = imageDTO.getImageUrl();
                StudyPostImage studyPostImage = new StudyPostImage(imageUrl);
                studyPost.addImage(studyPostImage); // image id가 저장되지 않음
                studyPostImage = studyPostImageRepository.save(studyPostImage);
                studyPost.updateImage(studyPostImage); // image id 저장
            }
        }

        if (studyPost.getIsAnnouncement()){

            // 스터디에 참여중인 회원들에게 알림 전송 위해 회원 조회
            List<Member> members = memberStudyRepository.findAllByStudyIdAndStatus(
                studyPost.getStudy().getId(), ApplicationStatus.APPROVED).stream()
                    .map(MemberStudy::getMember)
                        .toList();

            if (members.isEmpty())
                throw new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND);

            // 알림 생성
            for (Member studyMember : members) {
                Notification notification = Notification.builder()
                    .study(studyPost.getStudy())
                    .member(studyMember)
                    .notifierName(member.getName()) // 글을 작성한 회원 이름
                    .type(NotifyType.ANNOUNCEMENT)
                    .isChecked(false)
                    .build();
                notificationRepository.save(notification);
            }
        }

        member.updateStudyPost(studyPost);
        study.updateStudyPost(studyPost);

        return StudyPostResDTO.PostPreviewDTO.toDTO(studyPost);
    }

    @Override
    public StudyPostResDTO.PostPreviewDTO deletePost(Long studyId, Long postId) {

        //=== Exception ===//
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        StudyPost studyPost = studyPostRepository.findById(postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 로그인 회원이 게시글 작성자인지 확인
        studyPostRepository.findByIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_DELETION_INVALID));

        //=== Feature ===//
        List<StudyPostImage> imagesCopy = new ArrayList<>(studyPost.getImages());
        imagesCopy.forEach(image -> {
            studyPost.deleteImage(image);
            s3ImageService.deleteImageFromS3(image.getUrl());
            studyPostImageRepository.delete(image);
        });
        List<StudyPostComment> commentsCopy = new ArrayList<>(studyPost.getComments());
        commentsCopy.forEach(comment -> {
            studyPost.deleteComment(comment);
            studyPostCommentRepository.delete(comment);
        });
        List<StudyLikedPost> likedPostsCopy = new ArrayList<>(studyPost.getLikedPosts());
        likedPostsCopy.forEach(likedPost -> {
            studyPost.deleteLikedPost(likedPost);
            studyLikedPostRepository.delete(likedPost);
        });

        member.deleteStudyPost(studyPost);
        study.deleteStudyPost(studyPost);
        studyPostRepository.delete(studyPost);

        return StudyPostResDTO.PostPreviewDTO.toDTO(studyPost);
    }

    @Override
    public StudyPostResDTO.PostLikeNumDTO likePost(Long studyId, Long postId) {

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
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 이미 좋아요를 눌렀다면 다시 좋아요 할 수 없음
        if (studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId).isPresent()) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_ALREADY_LIKED);
        }

        //=== Feature ===//
        StudyLikedPost studyLikedPost = StudyLikedPost.builder()
                .member(member)
                .studyPost(studyPost)
                .build();

        studyLikedPost = studyLikedPostRepository.save(studyLikedPost);
        member.addStudyLikedPost(studyLikedPost);
        studyPost.addLikedPost(studyLikedPost);

        studyPost.plusLikeNum();
        studyPost = studyPostRepository.save(studyPost);

        return StudyPostResDTO.PostLikeNumDTO.toDTO(studyPost);
    }

    @Override
    public StudyPostResDTO.PostLikeNumDTO cancelPostLike(Long studyId, Long postId) {

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
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        //=== Feature ===//
        StudyLikedPost studyLikedPost = studyLikedPostRepository.findByMemberIdAndStudyPostId(memberId, postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_LIKED_POST_NOT_FOUND));

        member.deleteStudyLikedPost(studyLikedPost);
        studyPost.deleteLikedPost(studyLikedPost);
        studyPost.minusLikeNum();
        studyLikedPostRepository.delete(studyLikedPost);
        studyPostRepository.save(studyPost);

        return StudyPostResDTO.PostLikeNumDTO.toDTO(studyPost);
    }

/* ----------------------------- 스터디 게시글 댓글 관련 API ------------------------------------- */

    @Override
    public StudyPostCommentResponseDTO.CommentDTO createComment(Long studyId, Long postId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO) {

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
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        //=== Feature ===//
        Integer anonymousNum = getAnonymousNum(postId, commentRequestDTO, member);

        StudyPostComment studyPostComment = StudyPostComment.builder()
                .studyPost(studyPost)
                .member(member)
                .content(commentRequestDTO.getContent())
                .isAnonymous(commentRequestDTO.getIsAnonymous())
                .parentComment(null)
                .anonymousNum(anonymousNum)
                .build();

        studyPostCommentRepository.save(studyPostComment);
        studyPost.addComment(studyPostComment);
        member.addComment(studyPostComment);

        return StudyPostCommentResponseDTO.CommentDTO.toDTO(studyPostComment, "익명"+anonymousNum, defaultImage);
    }

    @Override
    public StudyPostCommentResponseDTO.CommentDTO createReply(Long studyId, Long postId, Long commentId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO) {

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
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findByIdAndStudyId(postId, studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 부모 댓글이 존재하는지 확인
        StudyPostComment parentComment = studyPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_NOT_FOUND));

        //=== Feature ===//
        Integer anonymousNum = getAnonymousNum(postId, commentRequestDTO, member);

        StudyPostComment studyPostComment = StudyPostComment.builder()
                .studyPost(studyPost)
                .member(member)
                .content(commentRequestDTO.getContent())
                .isAnonymous(commentRequestDTO.getIsAnonymous())
                .anonymousNum(anonymousNum)
                .parentComment(parentComment)
                .build();

        studyPostCommentRepository.save(studyPostComment);
        studyPost.addComment(studyPostComment);
        member.addComment(studyPostComment);
        parentComment.addChildrenComment(studyPostComment);

        return StudyPostCommentResponseDTO.CommentDTO.toDTO(studyPostComment, "익명"+anonymousNum, defaultImage);
    }

    private Integer getAnonymousNum(Long postId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO, Member member) {
        Integer anonymousNum = null;

        List<StudyPostComment> studyPostComments = studyPostCommentRepository.findByStudyPostId(postId);
        List<StudyPostComment> myStudyPostComments = studyPostCommentRepository.findByMemberIdAndStudyPostId(member.getId(), postId);

        // 회원이 익명 댓글을 요청할 경우 anonymousNum 부여
        if (commentRequestDTO.getIsAnonymous()) {
            // anonymousNum의 (최댓값+1) 계산
            int maxAnonymousNum = 0;
            for (StudyPostComment studyPostComment : studyPostComments) {
                if (studyPostComment.getAnonymousNum() != null && studyPostComment.getAnonymousNum() > maxAnonymousNum) {
                    maxAnonymousNum = studyPostComment.getAnonymousNum();
                }
            }
            anonymousNum = maxAnonymousNum+1;
            // 회원의 댓글 이력이 존재하는 경우 익명 작성 여부 확인
            // 해당 post에 익명으로 댓글을 남긴 이력이 있으면 해당 번호를 가져옴
            if (!myStudyPostComments.isEmpty()) {
                for (StudyPostComment myStudyPostComment : myStudyPostComments) {
                    if (myStudyPostComment.getIsAnonymous()) {
                        anonymousNum = myStudyPostComment.getAnonymousNum();
                    }
                }
                // 댓글은 있지만 익명으로 댓글을 남긴 이력이 없으면 그대로 최댓값+1 부여
            }
        }
        return anonymousNum;
    }

    @Override
    public StudyPostCommentResponseDTO.CommentIdDTO deleteComment(Long studyId, Long postId, Long commentId) {

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
        StudyPostComment studyPostComment = studyPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_NOT_FOUND));

        // 댓글 작성자인지 확인
        if(!studyPostComment.getMember().equals(member)) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_DELETE_INVALID);
        }

        //=== Feature ===//

        if (studyPostComment.getIsDeleted()) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_ALREADY_DELETED);
        }
        studyPostComment.deleteComment();
        studyPost.updateComment(studyPostComment);
        member.updateComment(studyPostComment);

        studyPostCommentRepository.save(studyPostComment);
        return new StudyPostCommentResponseDTO.CommentIdDTO(commentId);
    }

    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO likeComment(Long studyId, Long postId, Long commentId) {
        StudyPostComment studyPostComment = saveStudyPostComment(studyId, postId, commentId, Boolean.TRUE);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO dislikeComment(Long studyId, Long postId, Long commentId) {
        StudyPostComment studyPostComment = saveStudyPostComment(studyId, postId, commentId, Boolean.FALSE);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    private StudyPostComment saveStudyPostComment(Long studyId, Long postId, Long commentId, Boolean isLiked) {

        //=== Exception ===//

        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        StudyPostComment studyPostComment = studyPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 해당 스터디의 게시글인지 확인
        studyPostRepository.findById(postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));

        // 이미 좋아요나 싫어요를 눌렀다면 싫어요 할 수 없음
        if (studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, Boolean.TRUE).isPresent()) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_ALREADY_LIKED);
        }
        if (studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, Boolean.FALSE).isPresent()) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_ALREADY_DISLIKED);
        }

        //=== Feature ===//
        StudyLikedComment studyLikedComment = StudyLikedComment.builder()
                .studyPostComment(studyPostComment)
                .member(member)
                .isLiked(isLiked)
                .build();

        studyLikedComment = studyLikedCommentRepository.save(studyLikedComment);
        member.addStudyLikedComment(studyLikedComment);
        studyPostComment.addLikedComment(studyLikedComment);

        if (studyLikedComment.getIsLiked()) {
            studyPostComment.plusLikeCount();
        } else {
            studyPostComment.plusDislikeCount();
        }

        studyPostCommentRepository.save(studyPostComment);
        return studyPostComment;
    }

    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentLike(Long studyId, Long postId, Long commentId) {

        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        StudyLikedComment studyLikedComment = studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, Boolean.TRUE)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_LIKED_COMMENT_NOT_FOUND));

        StudyPostComment studyPostComment = deleteStudyLikedComment(studyId, postId, commentId, memberId, studyLikedComment);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentDislike(Long studyId, Long postId, Long commentId) {

        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        StudyLikedComment studyLikedComment = studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, Boolean.FALSE)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_DISLIKED_COMMENT_NOT_FOUND));

        StudyPostComment studyPostComment = deleteStudyLikedComment(studyId, postId, commentId, memberId, studyLikedComment);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    private StudyPostComment deleteStudyLikedComment(Long studyId, Long postId, Long commentId, Long memberId, StudyLikedComment studyLikedComment) {

        //=== Exception ===//
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        studyPostRepository.findById(postId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_NOT_FOUND));
        StudyPostComment studyPostComment = studyPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_NOT_FOUND));

        // 로그인한 회원이 스터디 회원인지 확인
        memberStudyRepository.findByMemberIdAndStudyIdAndStatus(member.getId(), studyId, ApplicationStatus.APPROVED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

        // 로그인한 회원이 댓글에 반응한 사람인지 확인
        if (!studyLikedComment.getMember().equals(member)) {
            throw new StudyHandler(ErrorStatus._STUDY_POST_COMMENT_DELETE_INVALID);
        }

        //=== Feature ===//
        member.deleteStudyLikedComment(studyLikedComment);
        studyPostComment.deleteLikedComment(studyLikedComment);

        if (studyLikedComment.getIsLiked()) {
            studyPostComment.minusLikeCount();
        } else {
            studyPostComment.minusDislikeCount();
        }

        studyLikedCommentRepository.delete(studyLikedComment);
        studyPostCommentRepository.save(studyPostComment);
        return studyPostComment;
    }

}

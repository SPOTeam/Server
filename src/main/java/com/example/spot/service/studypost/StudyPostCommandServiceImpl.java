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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyPostCommandServiceImpl implements StudyPostCommandService {

    @Value("${image.post.anonymous.profile}")
    private String defaultImage;

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;

    private final MemberStudyRepository memberStudyRepository;
    private final StudyPostRepository studyPostRepository;
    private final StudyPostImageRepository studyPostImageRepository;
    private final StudyPostCommentRepository studyPostCommentRepository;
    private final StudyLikedPostRepository studyLikedPostRepository;
    private final StudyLikedCommentRepository studyLikedCommentRepository;
    private final StudyPostReportRepository studyPostReportRepository;
    private final NotificationRepository notificationRepository;

    // S3 Service
    private final S3ImageService s3ImageService;

/* ----------------------------- 스터디 게시글 관련 API ------------------------------------- */

    /**
     * 스터디 내부 게시판에 게시글을 작성하는 메서드입니다.
     * @param studyId 게시글을 작성할 타겟 스터디의 아이디를 입력 받습니다.
     * @param postRequestDTO 게시글의 입력 형식(StudyPostRequestDTO.PostDTO)에 맞추어 게시글 정보를 입력 받습니다.
     * @return 작성된 스터디 게시글의 Preview(게시글 아이디, 제목)를 반환합니다.
     */
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
                .likeNum(0)
                .hitNum(0)
                .commentNum(0)
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

    /**
     * 스터디 내부 게시판에 작성된 게시글을 삭제합니다.
     * @param studyId 게시글을 삭제할 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 삭제할 스터디 게시글의 아이디를 입력 받습니다.
     * @return 삭제된 스터디 게시글의 Preview(게시글 아이디, 제목)를 반환합니다.
     */
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
        studyPostImageRepository.deleteAllByStudyPostId(postId);
        studyPostCommentRepository.deleteAllByStudyPostId(postId);
        studyLikedPostRepository.deleteAllByStudyPostId(postId);
        studyPostReportRepository.deleteAllByStudyPostId(postId);

        member.deleteStudyPost(studyPost);
        study.deleteStudyPost(studyPost);
        studyPostRepository.delete(studyPost);

        return StudyPostResDTO.PostPreviewDTO.toDTO(studyPost);
    }

    /**
     * 스터디 내부 게시판에 작성된 게시글에 좋아요를 누르는 메서드입니다.
     * 게시글에 좋아요를 누른 회원의 정보가 StudyLikedPost에 저장되고 스터디 게시글의 좋아요 개수가 업데이트 됩니다.
     * @param studyId 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 좋아요를 누를 타겟 게시글의 아이디를 입력 받습니다.
     * @return 게시글의 Preview(게시글 아이디, 제목)와 함께 좋아요 개수가 반환됩니다.
     */
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

    /**
     * 스터디 내부 게시판에 작성된 게시글에 누른 좋아요를 취소하는 메서드입니다.
     * 게시글에 좋아요를 누른 회원의 정보가 StudyLikedPost에서 삭제되고 스터디 게시글의 좋아요 개수가 업데이트 됩니다.
     * @param studyId 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 좋아요를 취소할 타겟 게시글의 아이디를 입력 받습니다.
     * @return 게시글의 Preview(게시글 아이디, 제목)와 함께 좋아요 개수가 반환됩니다.
     */
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

    /**
     * 스터디 게시글에 댓글을 추가하는 메서드입니다. 답글 추가 메서드는 하단에 별도로 구현되어 있습니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디를 입력 받습니다.
     * @param postId 댓글을 추가할 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentRequestDTO 추가할 댓글(내용, 익명 여부)을 입력 받습니다.
     * @return 댓글 아이디와 작성자, 내용, 좋아요와 싫어요 개수를 함께 반환합니다.
     */
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
                .likeCount(0)
                .dislikeCount(0)
                .isAnonymous(commentRequestDTO.getIsAnonymous())
                .parentComment(null)
                .isDeleted(false)
                .anonymousNum(anonymousNum)
                .build();

        studyPostCommentRepository.save(studyPostComment);

        studyPost.setCommentNum(studyPostCommentRepository.findAllByStudyPostId(postId).size());
        studyPostRepository.save(studyPost);

        studyPost.addComment(studyPostComment);
        member.addComment(studyPostComment);

        return StudyPostCommentResponseDTO.CommentDTO.toDTO(studyPostComment, "익명"+anonymousNum, defaultImage);
    }

    /**
     * 스터디 게시글에 답글을 추가하는 메서드입니다. 댓글 추가 메서드는 상단에 별도로 구현되어 있습니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디를 입력 받습니다.
     * @param postId 댓글을 추가할 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentRequestDTO 추가할 댓글(내용, 익명 여부)을 입력 받습니다.
     * @return 댓글 아이디와 작성자, 내용, 좋아요와 싫어요 개수를 함께 반환합니다.
     */
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
                .likeCount(0)
                .dislikeCount(0)
                .isAnonymous(commentRequestDTO.getIsAnonymous())
                .anonymousNum(anonymousNum)
                .parentComment(parentComment)
                .isDeleted(false)
                .build();

        studyPostCommentRepository.save(studyPostComment);

        studyPost.setCommentNum(studyPostCommentRepository.findAllByStudyPostId(postId).size());
        studyPostRepository.save(studyPost);

        studyPost.addComment(studyPostComment);
        member.addComment(studyPostComment);
        parentComment.addChildrenComment(studyPostComment);

        return StudyPostCommentResponseDTO.CommentDTO.toDTO(studyPostComment, "익명"+anonymousNum, defaultImage);
    }

    /**
     * 스터디 게시글 댓글마다 익명 번호를 부여하는 메서드입니다.
     * 회원이 이미 타겟 스터디 게시글에 익명으로 댓글을 작성한 이력이 있는 경우 해당 번호를 반환합니다.
     * @param postId 댓글을 작성할 타겟 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentRequestDTO 추가할 댓글(내용, 익명 여부)을 입력 받습니다.
     * @param member 댓글 작성자를 입력 받습니다.
     * @return 댓글 작성자의 익명 번호를 반환합니다.
     *         회원이 이미 타겟 스터디 게시글에 익명으로 댓글을 작성한 이력이 있는 경우 해당 번호를 반환합니다.
     */
    private Integer getAnonymousNum(Long postId, StudyPostCommentRequestDTO.CommentDTO commentRequestDTO, Member member) {
        Integer anonymousNum = null;

        List<StudyPostComment> studyPostComments = studyPostCommentRepository.findAllByStudyPostId(postId);
        List<StudyPostComment> myStudyPostComments = studyPostCommentRepository.findAllByMemberIdAndStudyPostId(member.getId(), postId);

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

    /**
     * 스터디 게시글에 작성한 댓글을 삭제하는 메서드입니다. 댓글 삭제와 답글 삭제 모두 해당 메서드를 활용합니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 댓글을 삭제할 타겟 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentId 삭제할 댓글의 아이디를 입력 받습니다.
     * @return 삭제한 댓글의 아이디를 반환합니다.
     */
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

    /**
     * 댓글에 좋아요를 누르는 메서드입니다. 댓글 좋아요와 답글 좋아요 모두 해당 메서드를 활용합니다.
     * 댓글에 좋아요를 누른 회원의 정보가 StudyLikedComment에 저장되고 타겟 댓글의 좋아요 개수가 업데이트 됩니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 타겟이 되는 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentId 좋아요를 누를 타겟 댓글의 아이디를 입력 받습니다.
     * @return 댓글 아이디와 타겟 댓글의 좋아요 수와 싫어요 수가 반환됩니다.
     */
    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO likeComment(Long studyId, Long postId, Long commentId) {
        StudyPostComment studyPostComment = saveStudyPostComment(studyId, postId, commentId, Boolean.TRUE);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    /**
     * 댓글에 싫어요를 누르는 메서드입니다. 댓글 싫어요와 답글 싫어요 모두 해당 메서드를 활용합니다.
     * 댓글에 싫어요를 누른 회원의 정보가 StudyLikedComment에 저장되고 타겟 댓글의 싫어요 개수가 업데이트 됩니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 타겟이 되는 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentId 싫어요를 누를 타겟 댓글의 아이디를 입력 받습니다.
     * @return 댓글 아이디와 타겟 댓글의 좋아요 수와 싫어요 수가 반환됩니다.
     */
    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO dislikeComment(Long studyId, Long postId, Long commentId) {
        StudyPostComment studyPostComment = saveStudyPostComment(studyId, postId, commentId, Boolean.FALSE);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    /**
     * 댓글 좋아요/싫어요 메서드에서 사용되는 내부 메서드입니다.
     * isLiked = true면 좋아요 정보를, isLiked = false면 싫어요 정보를 DB에 저장합니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 타겟이 되는 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentId 좋아요 혹은 싫어요를 누를 타겟 댓글의 아이디를 입력 받습니다.
     * @param isLiked 좋아요 혹은 싫어요 어부를 입력 받습니다.
     * @return SavePostComment 객체를 반환합니다.
     */
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

    /**
     * 댓글 좋아요를 취소하는 메서드입니다. 댓글 좋아요와 답글 좋아요 모두 해당 메서드를 활용합니다.
     * 댓글 좋아요를 취소한 회원의 정보가 StudyLikedComment에서 삭제되고 타겟 댓글의 싫어요 개수가 업데이트 됩니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 타겟이 되는 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentId 싫어요를 취소할 타겟 댓글의 아이디를 입력 받습니다.
     * @return 댓글 아이디와 타겟 댓글의 좋아요 수와 싫어요 수가 반환됩니다.
     */
    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentLike(Long studyId, Long postId, Long commentId) {

        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        StudyLikedComment studyLikedComment = studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, Boolean.TRUE)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_LIKED_COMMENT_NOT_FOUND));

        StudyPostComment studyPostComment = deleteStudyLikedComment(studyId, postId, commentId, memberId, studyLikedComment);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    /**
     * 댓글 싫어요를 취소하는 메서드입니다. 댓글 싫어요와 답글 싫어요 모두 해당 메서드를 활용합니다.
     * 댓글 싫어요를 취소한 회원의 정보가 StudyLikedComment에서 삭제되고 타겟 댓글의 싫어요 개수가 업데이트 됩니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 타겟이 되는 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentId 싫어요를 취소할 타겟 댓글의 아이디를 입력 받습니다.
     * @return 댓글 아이디와 타겟 댓글의 좋아요 수와 싫어요 수가 반환됩니다.
     */
    @Override
    public StudyPostCommentResponseDTO.CommentPreviewDTO cancelCommentDislike(Long studyId, Long postId, Long commentId) {

        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        StudyLikedComment studyLikedComment = studyLikedCommentRepository.findByMemberIdAndStudyPostCommentIdAndIsLiked(memberId, commentId, Boolean.FALSE)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_DISLIKED_COMMENT_NOT_FOUND));

        StudyPostComment studyPostComment = deleteStudyLikedComment(studyId, postId, commentId, memberId, studyLikedComment);
        return StudyPostCommentResponseDTO.CommentPreviewDTO.toDTO(studyPostComment);
    }

    /**
     * 댓글 좋아요/싫어요 취소 메서드에서 사용되는 내부 메서드입니다.
     * @param studyId 스터디 게시글이 작성된 타겟 스터디의 아이디를 입력 받습니다.
     * @param postId 타겟이 되는 스터디 게시글의 아이디를 입력 받습니다.
     * @param commentId 좋아요 혹은 싫어요를 취소할 타겟 댓글의 아이디를 입력 받습니다.
     * @param memberId 댓글에 좋아요 혹은 싫어요를 누른 회원의 아이디를 입력 받습니다.
     * @param studyLikedComment DB에서 삭제할 StudyLikedComment 객체를 입력 받습니다.
     * @return 삭제된 StudyLikedComment 객체를 반환합니다.
     */
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

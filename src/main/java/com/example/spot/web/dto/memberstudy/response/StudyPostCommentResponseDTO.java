package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.Member;
import com.example.spot.domain.mapping.StudyLikedComment;
import com.example.spot.domain.study.StudyPostComment;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@Getter
public class StudyPostCommentResponseDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class CommentDTO {

        private final Long commentId;
        private final MemberInfoDTO member;
        private final String content;
        private final Integer likeCount;
        private final Integer dislikeCount;

        public static CommentDTO toDTO(StudyPostComment comment, String name, String defaultImage) {
            return CommentDTO.builder()
                    .commentId(comment.getId())
                    .member(MemberInfoDTO.toDTO(comment.getMember(), name, comment.getIsAnonymous(), defaultImage))
                    .content(comment.getContent())
                    .likeCount(comment.getLikeCount())
                    .dislikeCount(comment.getDislikeCount())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    private static class MemberInfoDTO {

        private final Long memberId;
        private final String name;
        private final String profileImage;

        public static MemberInfoDTO toDTO(Member member, String anonymity, Boolean isAnonymous, String defaultImage) {
            if (isAnonymous) {
                return MemberInfoDTO.builder()
                        .memberId(member.getId())
                        .name(anonymity)
                        .profileImage(defaultImage)
                        .build();
            } else {
                return MemberInfoDTO.builder()
                        .memberId(member.getId())
                        .name(member.getName())
                        .profileImage(member.getProfileImage())
                        .build();
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CommentIdDTO {
        private final Long commentId;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class CommentPreviewDTO {

        private final Long commentId;
        private final Integer likeCount;
        private final Integer dislikeCount;

        public static CommentPreviewDTO toDTO(StudyPostComment comment) {
            return CommentPreviewDTO.builder()
                    .commentId(comment.getId())
                    .likeCount(comment.getLikeCount())
                    .dislikeCount(comment.getDislikeCount())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class CommentReplyListDTO {

        private final Long postId;
        private final List<CommentReplyDTO> comments;

        public static CommentReplyListDTO toDTO(Long postId, List<StudyPostComment> comments, Member member, String defaultImage) {
            return CommentReplyListDTO.builder()
                    .postId(postId)
                    .comments(comments.stream()
                            .map(comment -> CommentReplyDTO.toDTO(comment, member, defaultImage))
                            .toList())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class CommentReplyDTO {

        private final Long commentId;
        private final MemberInfoDTO member;
        private final String content;
        private final Integer likeCount;
        private final Integer dislikeCount;
        private final Boolean isDeleted;
        private final String isLiked;
        private final List<CommentReplyDTO> applies;

        public static CommentReplyDTO toDTO(StudyPostComment comment, Member member, String defaultImage) {

            String anonymity = "익명" + comment.getAnonymousNum();
            return CommentReplyDTO.builder()
                    .commentId(comment.getId())
                    .member(MemberInfoDTO.toDTO(comment.getMember(), anonymity, comment.getIsAnonymous(), defaultImage))
                    .content(comment.getContent())
                    .likeCount(comment.getLikeCount())
                    .dislikeCount(comment.getDislikeCount())
                    .isDeleted(comment.getIsDeleted())
                    .isLiked(getIsLiked(comment, member))
                    .applies(comment.getChildrenComment().stream()
                            .sorted(Comparator.comparing(StudyPostComment::getCreatedAt))
                            .map(child -> CommentReplyDTO.toDTO(child, member, defaultImage))
                            .toList())
                    .build();
        }

        private static String getIsLiked(StudyPostComment comment, Member member) {
            String isLiked = "NONE";
            for (StudyLikedComment likedComment : comment.getLikedComments()) {
                if (likedComment.getMember().equals(member)) {
                    if (likedComment.getIsLiked()) {
                        isLiked = "LIKED";
                    } else {
                        isLiked = "DISLIKED";
                    }
                    break;
                }
            }
            return isLiked;
        }
    }
}

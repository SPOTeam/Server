package com.example.spot.web.dto.study.response;

import com.example.spot.domain.Member;
import com.example.spot.domain.study.StudyPostComment;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

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
    public static class CommentPreviewDTO {
        private final Long commentId;
    }
}

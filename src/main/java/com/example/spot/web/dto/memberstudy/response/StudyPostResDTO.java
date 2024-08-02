package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.mapping.StudyPostImage;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class StudyPostResDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class PostPreviewDTO {

        private final Long postId;
        private final String title;

        public static PostPreviewDTO toDTO(StudyPost studyPost) {
            return PostPreviewDTO.builder()
                    .postId(studyPost.getId())
                    .title(studyPost.getTitle())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ImageListDTO {
        private final List<String> images;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class PostListDTO {

        private final Long studyId;
        private final List<PostDTO> posts;

        public static PostListDTO toDTO(Study study, List<PostDTO> postDTOS) {
            return PostListDTO.builder()
                    .studyId(study.getId())
                    .posts(postDTOS)
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class PostDTO {

        private final Long postId;
        private final String title;
        private final String content;
        private final Theme theme;
        private final Boolean isAnnouncement;
        private final LocalDateTime createdAt;
        private final Integer likeNum;
        private final Integer hitNum;
        private final Integer commentNum;

        public static PostDTO toDTO(StudyPost studyPost) {
            return PostDTO.builder()
                    .postId(studyPost.getId())
                    .title(studyPost.getTitle())
                    .content(studyPost.getContent())
                    .theme(studyPost.getTheme())
                    .isAnnouncement(studyPost.getIsAnnouncement())
                    .createdAt(studyPost.getCreatedAt())
                    .likeNum(studyPost.getLikeNum())
                    .hitNum(studyPost.getHitNum())
                    .commentNum(studyPost.getCommentNum())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class PostDetailDTO {

        private final Long memberId;
        private final Long postId;
        private final String title;
        private final String content;
        private final Theme theme;
        private final Boolean isAnnouncement;
        private final LocalDateTime createdAt;
        private final Integer likeNum;
        private final Integer hitNum;
        private final Integer commentNum;
        private final List<StudyPostImage> studyPostImages;

        public static PostDetailDTO toDTO(StudyPost studyPost) {
            return PostDetailDTO.builder()
                    .memberId(studyPost.getMember().getId())
                    .postId(studyPost.getId())
                    .title(studyPost.getTitle())
                    .content(studyPost.getContent())
                    .theme(studyPost.getTheme())
                    .isAnnouncement(studyPost.getIsAnnouncement())
                    .createdAt(studyPost.getCreatedAt())
                    .likeNum(studyPost.getLikeNum())
                    .hitNum(studyPost.getHitNum())
                    .commentNum(studyPost.getCommentNum())
                    .studyPostImages(studyPost.getImages())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static  class PostLikeNumDTO {

        private final Long postId;
        private final String title;
        private final Integer likeNum;

        public static PostLikeNumDTO toDTO(StudyPost studyPost) {
            return PostLikeNumDTO.builder()
                    .postId(studyPost.getId())
                    .title(studyPost.getTitle())
                    .likeNum(studyPost.getLikeNum())
                    .build();
        }

    }
}
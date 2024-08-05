package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.mapping.StudyPostImage;
import com.example.spot.domain.study.Study;
import com.example.spot.domain.study.StudyPost;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@Getter
public class StudyImageResponseDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class ImageListDTO {

        private final Long studyId;
        private final List<ImageDTO> images;

        public static ImageListDTO toDTO(Study study) {
            return ImageListDTO.builder()
                    .studyId(study.getId())
                    .images(study.getStudyPosts().stream()
                            .sorted(Comparator.comparing(StudyPost::getCreatedAt))
                            .flatMap(studyPost -> studyPost.getImages().stream())
                            .map(ImageDTO::toDTO)
                            .toList())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class ImageDTO {

        private final Long imageId;
        private final String imageUrl;
        private final Long postId;

        public static ImageDTO toDTO(StudyPostImage studyPostImage) {
            return ImageDTO.builder()
                    .postId(studyPostImage.getStudyPost().getId())
                    .imageId(studyPostImage.getId())
                    .imageUrl(studyPostImage.getUrl())
                    .build();
        }
    }
}

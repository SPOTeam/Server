package com.example.spot.web.dto.study.response;

import com.example.spot.domain.Post;
import com.example.spot.domain.study.StudyPost;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
}

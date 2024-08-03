package com.example.spot.web.dto.memberstudy.request;

import com.example.spot.domain.enums.Theme;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class StudyPostRequestDTO {

    @Getter
    @RequiredArgsConstructor
    @Schema(name = "StudyPostDTO")
    public static class PostDTO {

        @NotNull
        @Schema(description = "회원 ID", example = "1")
        private final Long memberId;

        @NotNull
        @Schema(description = "공지 여부", example = "false")
        private final Boolean isAnnouncement;

        @NotNull
        @Schema(description = "테마", example = "WELCOME")
        private final Theme theme;

        @NotNull
        @Schema(description = "제목", example = "title")
        private final String title;

        @NotNull
        @Schema(description = "내용", example = "content")
        private final String content;

        @NotNull
        @Schema(description = "이미지")
        private final List<MultipartFile> images;
    }
}

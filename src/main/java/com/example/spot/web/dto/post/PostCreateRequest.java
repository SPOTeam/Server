package com.example.spot.web.dto.post;

import com.example.spot.domain.enums.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @Schema(description = "게시글 제목입니다.",
            format = "string")
    @NotNull
    private String title;

    @Schema(description = "게시글 내용입니다.",
            format = "string")
    @NotNull
    private String content;

    @Schema(
            description = "게시글 타입입니다. " +
                    "PASS_EXPERIENCE, INFORMATION_SHARING, COUNSELING, JOB_TALK, FREE_TALK, SPOT_ANNOUNCEMENT 중에 작성해주세요.",
            example = "PASS_EXPERIENCE"
    )
    private Board type;

    @Schema(description = "익명 여부", example = "false")
    private boolean anonymous;

    @Schema(description = "이미지 파일", type = "string", format = "binary")
    private MultipartFile image;

}

package com.example.spot.web.dto.post;

import com.example.spot.domain.enums.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {

    @Schema(description = "게시글 제목입니다.",
            format = "string")
    private String title;

    @Schema(description = "게시글 내용입니다.",
            format = "string")
    private String content;

    @Schema(description = "익명 여부", example = "false")
    private boolean isAnonymous;

    @Schema(
            description = "게시글 타입입니다. 아래와 같이 작성해주세요.",
            allowableValues = {"ALL", "PASS_EXPERIENCE", "INFORMATION_SHARING", "COUNSELING", "JOB_TALK", "FREE_TALK", "SPOT_ANNOUNCEMENT"}
    )
    private String type;

    private MultipartFile image;

    private String existingImage;

    public Board getType() {
        return Board.findByValue(type);
    }
}

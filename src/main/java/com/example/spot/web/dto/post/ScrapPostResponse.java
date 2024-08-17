package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ScrapPostResponse {
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "스크랩 수", example = "10")
    private Long scrapCount;
}

package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostLikeResponse {
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "좋아요 수", example = "10")
    private Long likeCount;
}

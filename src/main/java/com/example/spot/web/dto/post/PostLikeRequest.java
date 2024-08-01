package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostLikeRequest {
    @Schema(description = "멤버 ID", example = "1")
    private Long memberId;

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;
}

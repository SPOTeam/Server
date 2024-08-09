package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentLikeResponse {

    @Schema(
            description = "댓글 ID",
            example = "1"
    )
    private Long commentId;

    @Schema(
            description = "좋아요 수", example = "10"
    )

    private long likeCount;

    public static CommentLikeResponse toDTO(Long commentId, long likeCount) {
        return CommentLikeResponse.builder()
                .commentId(commentId)
                .likeCount(likeCount)
                .build();
    }

}

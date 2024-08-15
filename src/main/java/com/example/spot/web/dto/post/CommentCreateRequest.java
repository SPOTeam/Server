package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    @Schema(description = "댓글 내용입니다.",
            format = "string")
    private String content;

    @Schema(description = "익명 여부", example = "false")
    private boolean anonymous;

    @Schema(
            description = "부모 댓글 ID (대댓글의 경우)",
            example = "1"
    )
    private Long parentCommentId;
}

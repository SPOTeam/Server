package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse {

    @Schema(
            description = "댓글 ID",
            example = "101"
    )
    private Long commentId;

    @Schema(
            description = "댓글 내용",
            example = "댓글 내용 예시"
    )
    private String commentContent;

    @Schema(
            description = "부모 댓글 ID (대댓글의 경우)",
            example = "1"
    )
    private Long parentId;

    @Schema(
            description = "댓글 작성자",
            example = "댓글 작성자 예시"
    )
    private String writer;

    @Schema(
            description = "작성 시간입니다.",
            type = "string",
            format = "date-time",
            example = "2023-06-23T10:15:30"
    )
    private String writtenTime;

}
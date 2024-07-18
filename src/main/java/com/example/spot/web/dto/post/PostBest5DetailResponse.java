package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostBest5DetailResponse {

    @Schema(
            description = "순위입니다.",
            format = "int"
    )
    private int rank;

    @Schema(
            description = "게시글 제목입니다.",
            format = "string"
    )
    private String postTitle;

    @Schema(
            description = "댓글 수입니다.",
            format = "int"
    )
    private int commentCount;
}

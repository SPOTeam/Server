package com.example.spot.web.dto.post;

import com.example.spot.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
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

    public static PostBest5DetailResponse from(Post post, int rank) {
        return PostBest5DetailResponse.builder()
                .rank(rank)
                .postTitle(post.getTitle())
                .commentCount(post.getPostCommentList().size())
                .build();
    }
}

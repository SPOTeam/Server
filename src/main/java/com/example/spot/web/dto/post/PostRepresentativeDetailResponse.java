package com.example.spot.web.dto.post;

import com.example.spot.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PostRepresentativeDetailResponse {
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "게시글 타입입니다.", example = "JOB_TALK")
    private String postType;

    @Schema(description = "게시글 제목입니다.", example = "게시글 제목")
    private String postTitle;

    @Schema(description = "댓글 수입니다.", example = "5")
    private int commentCount;

    public static PostRepresentativeDetailResponse toDTO(Post post) {
        return PostRepresentativeDetailResponse.builder()
                .postId(post.getId())
                .postType(post.getBoard().name())
                .postTitle(post.getTitle())
                .commentCount(post.getPostCommentList().size())
                .build();
    }
}

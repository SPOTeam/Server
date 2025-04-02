package com.example.spot.web.dto.post;

import com.example.spot.domain.PostComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CommentCreateResponse {
    @Schema(description = "댓글 ID입니다.", example = "1")
    private Long id;

    @Schema(description = "부모 댓글 ID (대댓글의 경우)", example = "1")
    private Long parentCommentId;

    @Schema(description = "댓글 내용입니다.", example = "댓글 내용")
    private String content;

    @Schema(description = "작성자 이름입니다.", example = "작성자")
    private String writer;

    public static CommentCreateResponse toDTO(PostComment comment) {
        return CommentCreateResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writer(comment.isAnonymous() ? "익명" : comment.getMember().getNickname())
                .build();
    }

    public static CommentCreateResponse toDTOwithParent(PostComment comment, Long parentCommentId) {
        return CommentCreateResponse.builder()
                .id(comment.getId())
                .parentCommentId(parentCommentId)
                .content(comment.getContent())
                .writer(comment.isAnonymous() ? "익명" : comment.getMember().getNickname())
                .build();
    }

}

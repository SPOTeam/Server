package com.example.spot.web.dto.post;

import com.example.spot.domain.PostComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentDetailResponse {
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
    private Long parentCommentId;

    @Schema(
            description = "댓글 작성자",
            example = "댓글 작성자 예시"
    )
    private String writer;

    @Schema(
            description = "작성 시간입니다.",
            type = "string",
            format = "date-time",
            example = "2024-07-19T10:15:30"
    )
    private String writtenTime;

    @Schema(
            description = "좋아요 수입니다.",
            format = "long"
    )
    private long likeCount;

//    @Schema(
//            description = "싫어요 수입니다.",
//            format = "int"
//    )
//    private int disLikeCount;

    public static CommentDetailResponse toDTO(PostComment comment, long likeCount) {
        // 작성자가 익명인지 확인하여 작성자 이름 설정
        String writerName = judgeAnonymous(comment.isAnonymous(), comment.getMember().getName());

        return CommentDetailResponse.builder()
                .commentId(comment.getId())
                .commentContent(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .writer(writerName)
                .writtenTime(comment.getCreatedAt().toString())
                .likeCount(likeCount)
                //.disLikeCount(comment.getDisLikeNum())
                .build();
    }
    public static String judgeAnonymous(Boolean isAnonymous, String writer) {

        if (isAnonymous) {
            return "익명";
        }

        return writer;
    }
}
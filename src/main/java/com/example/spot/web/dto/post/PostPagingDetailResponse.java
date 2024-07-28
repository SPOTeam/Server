package com.example.spot.web.dto.post;

import com.example.spot.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class PostPagingDetailResponse {

    @Schema(
            description = "작성자입니다.",
            format = "string"
    )
    private String writer;

    @Schema(
            description = "작성 시간입니다.",
            type = "string",
            format = "date-time",
            example = "2023-06-23T10:15:30"
    )
    private LocalDateTime writtenTime;

    @Schema(
            description = "스크랩 수입니다.",
            format = "int"
    )
    private int scrapCount;

    @Schema(description = "게시글 제목입니다.",
            format = "string")
    private String title;

    @Schema(description = "게시글 내용입니다.",
            format = "string")
    private String content;

    @Schema(
            description = "좋아요 수입니다.",
            format = "int"
    )
    private int likeCount;

    @Schema(
            description = "댓글 수입니다.",
            format = "int"
    )
    private int commentCount;

    @Schema(
            description = "조회 수입니다.",
            format = "int"
    )
    private int viewCount;

    public static String judgeAnonymous(Boolean isAnonymous, String writer) {

        if (isAnonymous) {
            return "익명";
        }
        return writer;
    }
    public static PostPagingDetailResponse toDTO(Post post) {
        // 작성자가 익명인지 확인하여 작성자 이름 설정
        String writerName = judgeAnonymous(post.isAnonymous(), post.getMember().getName());

        return PostPagingDetailResponse.builder()
                .writer(writerName)
                .writtenTime(post.getCreatedAt())
                .scrapCount(post.getScrapNum())
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeNum())
                .commentCount(post.getCommentNum())
                .viewCount(post.getHitNum())
                .build();
    }
}

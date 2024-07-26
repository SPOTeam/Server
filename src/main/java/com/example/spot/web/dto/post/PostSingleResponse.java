package com.example.spot.web.dto.post;

import com.example.spot.domain.Post;
import com.example.spot.domain.enums.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@AllArgsConstructor
public class PostSingleResponse {

    @Schema(
            description = "게시글 타입입니다. 아래와 같이 작성해주세요.",
            allowableValues = {"ALL", "PASS_EXPERIENCE", "INFORMATION_SHARING", "COUNSELING", "JOB_TALK", "FREE_TALK", "SPOT_ANNOUNCEMENT"}
    )
    private String type;

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

    @Schema(
            description = "사진 url입니다.",
            format = "string"
    )
    private List<String> fileUrls;

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

    @Schema(
            description = "댓글 리스트입니다.",
            format = "array"
    )
    private List<CommentResponse> commentResponses;

    @Schema(
            description = "신고 여부입니다.",
            format = "boolean"
    )
    private boolean isReported;

    public Board getType() {
        return Board.findByValue(type);
    }

    public static String judgeAnonymous(Boolean isAnonymous, String writer) {

        if (isAnonymous) {
            return "익명";
        }
        return writer;
    }

    public static PostSingleResponse toDTO(Post post) {
        // 작성자가 익명인지 확인하여 작성자 이름 설정
        String writerName = judgeAnonymous(post.isAnonymous(), post.getMember().getName());

        return PostSingleResponse.builder()
                .type(post.getBoard().name())
                .writer(writerName)
                .writtenTime(post.getCreatedAt())
                .scrapCount(post.getMemberScrapList().size())
                //.fileUrls(post.getPostImageList().stream().map(PostImage::getUrl).toList())
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeNum())
                .commentCount(post.getPostCommentList().size())
                .viewCount(post.getHitNum())
                .commentResponses(post.getPostCommentList().stream().map(CommentResponse::toDTO).toList())
                .build();
    }
}

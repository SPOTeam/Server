package com.example.spot.web.dto.post;

import com.example.spot.domain.Post;
import com.example.spot.domain.enums.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class PostSingleResponse {

    @Schema(
            description = "게시글 타입입니다."
    )
    private String type;

    @Schema(
            description = "작성자입니다.",
            format = "string"
    )
    private String writer;

    @Schema(
            description = "게시글 작성자 익명 여부입니다."
    )
    private Boolean anonymous;

    @Schema(
            description = "댓글 작성자 프로필 사진입니다."
    )
    private String profileImage;

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
    private Long scrapCount;

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
    private Long likeCount;

    @Schema(
            description = "댓글 수입니다.",
            format = "int"
    )
    private Integer commentCount;

    @Schema(
            description = "조회 수입니다.",
            format = "int"
    )
    private Integer viewCount;

    @Schema(
            description = "현재 사용자의 해당 게시글 좋아요 여부입니다."
    )
    private Boolean likedByCurrentUser;

    @Schema(
            description = "현재 사용자의 해당 게시글 스크랩 여부입니다."
    )
    private Boolean scrapedByCurrentUser;

    @Schema(
            description = "현재 사용자의 해당 게시글 작성 여부입니다."
    )
    private Boolean createdByCurrentUser;

    @Schema(
            description = "댓글 리스트입니다.",
            format = "array"
    )
    private CommentResponse commentResponses;

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

    public static String anonymousProfileImage(Boolean isAnonymous, String profileImage, String defaultProfileImageUrl) {

        if (isAnonymous) {
            return defaultProfileImageUrl;
        }

        return profileImage;
    }

    public static PostSingleResponse toDTO(Post post, long likeCount, long scrapCount, CommentResponse commentResponse, boolean likedByCurrentUser, boolean scrapedByCurrentUser, boolean createdByCurrentUser, String defaultProfileImageUrl) {
        // 작성자가 익명인지 확인하여 작성자 이름 설정
        String writerName = judgeAnonymous(post.isAnonymous(), post.getMember().getName());
        // 작성자가 익명인지 확인하여 프로필 반환
        String writerImage = anonymousProfileImage(post.isAnonymous(), post.getMember().getProfileImage(), defaultProfileImageUrl);

        return PostSingleResponse.builder()
                .type(post.getBoard().name())
                .writer(writerName)
                .anonymous(post.isAnonymous())
                .profileImage(writerImage)
                .writtenTime(post.getCreatedAt())
                .scrapCount(scrapCount)
                .scrapedByCurrentUser(scrapedByCurrentUser)
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(likeCount)
                .likedByCurrentUser(likedByCurrentUser)
                .createdByCurrentUser(createdByCurrentUser)
                .commentCount(commentResponse.getComments().size())
                .viewCount(post.getHitNum())
                .commentResponses(commentResponse)
                .build();
    }
}

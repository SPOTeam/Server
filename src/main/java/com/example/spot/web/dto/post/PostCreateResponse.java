package com.example.spot.web.dto.post;

import com.example.spot.domain.Member;
import com.example.spot.domain.Post;
import com.example.spot.domain.enums.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "생성된 게시글 제목입니다.",
            format = "string")
    private String title;

    @Schema(description = "생성된 게시글 내용입니다.",
            format = "string")
    private String content;

    @Schema(
            description = "생성된 게시글 타입입니다.",
            allowableValues = {"ALL", "PASS_EXPERIENCE", "INFORMATION_SHARING", "COUNSELING", "JOB_TALK", "FREE_TALK", "SPOT_ANNOUNCEMENT"}
    )
    private String type;

    @Schema(description = "관리자 여부", example = "false")
    private boolean isAdmin;

    @Schema(description = "익명 여부", example = "false")
    private boolean isAnonymous;

    @Schema(description = "좋아요 수", example = "0")
    private Long likeNum;

    @Schema(description = "댓글 수", example = "0")
    private int commentNum;

    @Schema(description = "조회수", example = "0")
    private int hitNum;

    @Schema(description = "생성 시간", example = "2024-01-01T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2024-01-01T12:34:56")
    private LocalDateTime updatedAt;

    public Board getType() {
        return Board.findByValue(type);
    }

    public static PostCreateResponse toDTO(Post post, Boolean isAdmin, long likeCount) {

        return PostCreateResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .isAdmin(isAdmin)
                .isAnonymous(post.isAnonymous())
                .likeNum(likeCount)
                .commentNum(post.getCommentNum())
                .hitNum(post.getHitNum())
                .type(post.getBoard().name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

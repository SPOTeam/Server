package com.example.spot.web.dto.post;

import com.example.spot.domain.enums.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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


    public Board getType() {
        return Board.findByValue(type);
    }
}

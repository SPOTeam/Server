package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostPagingResponse {

    @Schema(
            description = "게시글 타입입니다. 아래와 같이 작성해주세요.",
            allowableValues = {"ALL", "PASS_EXPERIENCE", "INFORMATION_SHARING", "COUNSELING", "JOB_TALK", "FREE_TALK", "SPOT_ANNOUNCEMENT"}
    )
    private String postType;

    @Schema(
            description = "게시글 상세 응답 리스트입니다.",
            format = "array"
    )
    private List<PostPagingDetailResponse> postResponses;

    @Schema(
            description = "전체 페이지 수입니다.",
            format = "int"
    )
    private Integer totalPage;

    @Schema(
            description = "전체 게시글 수입니다.",
            format = "long"
    )
    private Long totalElements;

    @Schema(
            description = "첫 번째 페이지 여부입니다.",
            format = "boolean"
    )
    private Boolean isFirst;

    @Schema(
            description = "마지막 페이지 여부입니다.",
            format = "boolean"
    )
    private Boolean isLast;

}

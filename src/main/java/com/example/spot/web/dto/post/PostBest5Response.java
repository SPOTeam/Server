package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostBest5Response {

    @Schema(
            description = "인기글 종류입니다. REAL_TIME, RECOMMEND, COMMENT 중 하나입니다.",
            example = "REAL_TIME"
    )
    private String sortType;

    @Schema(
            description = "인기글 목록입니다.",
            type = "array"
    )
    private List<PostBest5DetailResponse> postBest5Responses;
}

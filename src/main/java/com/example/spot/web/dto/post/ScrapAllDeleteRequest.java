package com.example.spot.web.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScrapAllDeleteRequest {
    @Schema (description = "삭제할 게시글 Id 리스트 입니다.")
    private List<Long> deletePostIds;
}

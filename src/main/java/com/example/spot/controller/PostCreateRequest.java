package com.example.spot.controller;

import com.example.spot.domain.enums.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostCreateRequest {
    @Schema(
            description = "게시글에 첨부할 이미지 파일 리스트. 파일 형식은 binary이며, " +
                    "지원되는 이미지 형식은 JPEG, PNG입니다. 크기는 10MB 이하로만 가능합니다.",
            format = "binary"
    )
    private List<MultipartFile> files;

    @Schema(description = "게시글 제목입니다.",
            format = "string")
    private String title;

    @Schema(description = "게시글 내용입니다.",
            format = "string")
    private String content;

    @Schema(
            description = "게시글 타입입니다. 아래와 같이 작성해주세요.",
            allowableValues = {"PASS_EXPERIENCE", "INFORMATION_SHARING", "COUNSELING", "JOB_TALK", "FREE_TALK", "SPOT_ANNOUNCEMENT"}
    )
    private String type;

    public Board getType() {
        return Board.findByValue(type);
    }
}

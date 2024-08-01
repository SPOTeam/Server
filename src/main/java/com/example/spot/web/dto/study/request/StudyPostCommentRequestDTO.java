package com.example.spot.web.dto.study.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class StudyPostCommentRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDTO {

        private Long memberId;
        private Boolean isAnonymous;
        private String content;
    }
}

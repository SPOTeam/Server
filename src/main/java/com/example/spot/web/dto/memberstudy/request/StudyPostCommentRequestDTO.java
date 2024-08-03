package com.example.spot.web.dto.memberstudy.request;

import com.example.spot.validation.annotation.TextLength;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class StudyPostCommentRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDTO {

        private Boolean isAnonymous;

        @TextLength(min = 1, max = 255)
        private String content;
    }
}

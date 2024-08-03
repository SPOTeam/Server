package com.example.spot.web.dto.memberstudy.request;

import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.validation.annotation.TextLength;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class StudyQuizRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizDTO {

        @TextLength(min = 1, max = 20)
        private String question;

        @TextLength(min = 1, max = 10)
        private String answer;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceDTO {

        @TextLength(min = 1, max = 10)
        private String answer;
    }
}

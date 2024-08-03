package com.example.spot.web.dto.memberstudy.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class StudyQuizRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizDTO {

        private String question;
        private String answer;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceDTO {

        private Long memberId;
        private String answer;
    }
}

package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.Quiz;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class StudyQuizResponseDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class QuizDTO {

        private final Long quizId;
        private final String question;

        public static QuizDTO toDTO(Quiz quiz) {
            return QuizDTO.builder()
                    .quizId(quiz.getId())
                    .question(quiz.getQuestion())
                    .build();
        }
    }
}

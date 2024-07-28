package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.Quiz;
import com.example.spot.domain.mapping.MemberAttendance;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

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

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class AttendanceDTO {

        private final Long memberId;
        private final Long quizId;
        private final Long attendanceId;
        private final Boolean isCorrect;
        private final LocalDateTime createdAt;

        public static AttendanceDTO toDTO(MemberAttendance memberAttendance) {
            return AttendanceDTO.builder()
                    .memberId(memberAttendance.getMember().getId())
                    .quizId(memberAttendance.getQuiz().getId())
                    .attendanceId(memberAttendance.getId())
                    .isCorrect(memberAttendance.getIsCorrect())
                    .createdAt(memberAttendance.getCreatedAt())
                    .build();
        }
    }

}

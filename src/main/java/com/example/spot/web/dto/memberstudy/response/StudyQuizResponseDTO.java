package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.Quiz;
import com.example.spot.domain.mapping.MemberAttendance;
import com.example.spot.domain.mapping.MemberStudy;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
        private final Integer tryNum;
        private final LocalDateTime createdAt;

        public static AttendanceDTO toDTO(MemberAttendance memberAttendance, Integer tryNum) {
            return AttendanceDTO.builder()
                    .memberId(memberAttendance.getMember().getId())
                    .quizId(memberAttendance.getQuiz().getId())
                    .attendanceId(memberAttendance.getId())
                    .isCorrect(memberAttendance.getIsCorrect())
                    .tryNum(tryNum)
                    .createdAt(memberAttendance.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class AttendanceListDTO {

        private final Long studyId;
        private final Long quizId;
        private final List<StudyMemberDTO> studyMembers;

        public static AttendanceListDTO toDTO(Quiz quiz, List<StudyMemberDTO> studyMembers) {
            return AttendanceListDTO.builder()
                    .studyId(quiz.getStudy().getId())
                    .quizId(quiz.getId())
                    .studyMembers(studyMembers)
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class StudyMemberDTO {

        private final Long memberId;
        private final String name;
        private final String profileImage;
        private final Boolean isOwned;
        private final Boolean isAttending;

        public static StudyMemberDTO toDTO(MemberStudy memberStudy, Boolean isAttending) {
            return StudyMemberDTO.builder()
                    .memberId(memberStudy.getMember().getId())
                    .name(memberStudy.getMember().getName())
                    .profileImage(memberStudy.getMember().getProfileImage())
                    .isOwned(memberStudy.getIsOwned())
                    .isAttending(isAttending)
                    .build();
        }

    }
}

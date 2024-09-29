package com.example.spot.api.code.status;

import com.example.spot.api.code.BaseCode;
import com.example.spot.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    //공통 응답
    _OK(HttpStatus.OK, "COMMON200", "OK"),
    _CREATED(HttpStatus.CREATED, "COMMON201", "생성 완료"),
    _ACCEPTED(HttpStatus.ACCEPTED, "COMMON202", "요청 수락됨"),
    _NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON204", "콘텐츠 없음"),

    //멤버 관련 응답
    _MEMBER_CREATED(HttpStatus.CREATED, "MEMBER2001", "회원 가입 및 로그인 완료"),
    _MEMBER_UPDATED(HttpStatus.OK, "MEMBER2002", "회원 정보 수정 완료"),
    _MEMBER_DELETED(HttpStatus.OK, "MEMBER2003", "회원 탈퇴 완료"),
    _MEMBER_FOUND(HttpStatus.OK, "MEMBER2004", "회원 조회 완료"),
    _MEMBER_LIST_FOUND(HttpStatus.OK, "MEMBER2005", "회원 목록 조회 완료"),
    _MEMBER_THEME_UPDATE(HttpStatus.OK, "MEMBER2006", "회원 테마 수정 완료"),
    _MEMBER_REGION_UPDATE(HttpStatus.OK, "MEMBER2007", "회원 지역 수정 완료"),
    _MEMBER_INFO_UPDATE(HttpStatus.OK, "MEMBER2008", "회원 정보 수정 완료"),
    _MEMBER_SIGNED_IN(HttpStatus.OK, "MEMBER2009", "회원 로그인 완료"),

    //스터디 게시글 관련 응답
    _STUDY_POST_CREATED(HttpStatus.CREATED, "STUDYPOST3001", "스터디 게시글 작성 완료"),
    _STUDY_POST_UPDATED(HttpStatus.OK, "STUDYPOST3002", "스터디 게시글 수정 완료"),
    _STUDY_POST_DELETED(HttpStatus.OK, "STUDYPOST3003", "스터디 게시글 삭제 완료"),
    _STUDY_POST_FOUND(HttpStatus.OK, "STUDYPOST3004", "스터디 게시글 조회 완료"),
    _STUDY_POST_LIST_FOUND(HttpStatus.OK, "STUDYPOST3005", "스터디 게시글 목록 조회 완료"),
    _STUDY_POST_LIKED(HttpStatus.CREATED, "STUDYPOST3006", "스터디 게시글 좋아요 완료"),
    _STUDY_POST_DISLIKED(HttpStatus.OK, "STUDYPOST3007", "스터디 게시글 좋아요 취소 완료"),
    _STUDY_POST_COMMENT_CREATED(HttpStatus.CREATED, "STUDYPOST3008", "스터디 게시글 댓글 작성 완료"),
    _STUDY_POST_COMMENT_UPDATED(HttpStatus.OK, "STUDYPOST3009", "스터디 게시글 댓글 수정 완료"),
    _STUDY_POST_COMMENT_DELETED(HttpStatus.OK, "STUDYPOST3010", "스터디 게시글 댓글 삭제 완료"),
    _STUDY_POST_COMMENT_FOUND(HttpStatus.OK, "STUDYPOST3011", "스터디 게시글 댓글 조회 완료"),
    _STUDY_POST_COMMENT_LIKED(HttpStatus.CREATED, "STUDYPOST3012", "스터디 게시글 댓글 좋아요 완료"),
    _STUDY_POST_COMMENT_DISLIKED(HttpStatus.CREATED, "STUDYPOST3013", "스터디 게시글 댓글 싫어요 완료"),
    _STUDY_POST_COMMENT_LIKE_CANCELED(HttpStatus.OK, "STUDYPOST3014", "스터디 게시글 댓글 좋아요 취소 완료"),
    _STUDY_POST_COMMENT_DISLIKE_CANCELED(HttpStatus.OK, "STUDYPOST3015", "스터디 게시글 댓글 싫어요 취소 완료"),

    //알림 관련 응답
    _NOTIFICATION_FOUND(HttpStatus.OK, "NOTIFICATION4001", "전체 알림 조회 완료"),
    _NOTIFICATION_READ(HttpStatus.OK, "NOTIFICATION4002", "알림 읽음 처리 완료"),
    _NOTIFICATION_APPLIED_STUDY_FOUND(HttpStatus.OK, "NOTIFICATION4002", "참가 신청한 스터디 알림 조회 완료"),
    _NOTIFICATION_APPLIED_STUDY_JOINED(HttpStatus.OK, "NOTIFICATION4003", "참가 신청한 스터디 최종 참여 확인 완료"),
    _NOTIFICATION_APPLIED_STUDY_REJECTED(HttpStatus.OK, "NOTIFICATION4004", "참가 신청한 스터디 최종 참여 거절 완료"),

    //스터디 관련
    _STUDY_CREATED(HttpStatus.CREATED, "STUDY4001", "스터디 생성 완료"),
    _STUDY_UPDATED(HttpStatus.OK, "STUDY4002", "스터디 수정 완료"),
    _STUDY_FOUND(HttpStatus.OK, "STUDY4003", "스터디 조회 완료"),
    _STUDY_MEMBER_CREATED(HttpStatus.CREATED, "STUDY4004", "스터디 참여 완료"),
    _STUDY_MEMBER_DELETED(HttpStatus.OK, "STUDY4005", "스터디 탈퇴 완료"),
    _STUDY_TERMINATED(HttpStatus.OK, "STUDY4006", "스터디 종료 완료"),
    _STUDY_LIKED(HttpStatus.OK, "STUDY4007", "스터디 찜 요청이 정상적 처리 되었습니다."),
    _STUDY_MEMBER_FOUND(HttpStatus.OK, "STUDY4009", "스터디 참여 회원 조회 완료"),
    _STUDY_APPLICANT_FOUND(HttpStatus.OK, "STUDY4010", "스터디 신청자 조회 완료"),
    _STUDY_APPLICANT_UPDATED(HttpStatus.OK, "STUDY4011", "스터디 신청 처리 완료"),
    _STUDY_APPLY_COMPLETED(HttpStatus.OK, "STUDY4012", "스터디 신청 완료"),

    //스터디 출석 퀴즈 관련
    _STUDY_QUIZ_CREATED(HttpStatus.CREATED, "QUIZ2001", "스터디 퀴즈 생성 완료"),
    _STUDY_ATTENDANCE_CREATED_CORRECT_ANSWER(HttpStatus.CREATED, "QUIZ2002", "스터디 출석 완료"),
    _STUDY_ATTENDANCE_CREATED_WRONG_ANSWER(HttpStatus.CREATED, "QUIZ2003", "스터디 퀴즈 오답"),
    _STUDY_QUIZ_DELETED(HttpStatus.OK, "QUIZ2004", "스터디 퀴즈 삭제 완료"),
    _STUDY_MEMBER_ATTENDANCES_FOUND(HttpStatus.OK, "QUIZ2005", "금일 회원 출석부 조회 완료"),

    // 스터디 일정 관련 응답
    _STUDY_SCHEDULE_CREATED(HttpStatus.CREATED, "SCHEDULE2001", "스터디 일정 생성 완료"),
    _STUDY_SCHEDULE_FOUND(HttpStatus.OK, "SCHEDULE2002", "스터디 일정 조회 완료"),
    _STUDY_SCHEDULE_UPDATED(HttpStatus.OK, "SCHEDULE2003", "스터디 일정 수정 완료"),

    // 스터디 갤러리 관련
    _STUDY_POST_IMAGES_FOUND(HttpStatus.OK, "GALLERY2001", "스터디 이미지 목록 조회 완료"),

    // 스터디 투표 관련 응답
    _STUDY_VOTE_CREATED(HttpStatus.CREATED, "VOTE2001", "스터디 투표 생성 완료"),
    _STUDY_VOTE_FOUND(HttpStatus.OK, "VOTE2002", "스터디 투표 조회 완료"),
    _STUDY_VOTE_DELETED(HttpStatus.OK, "VOTE2003", "스터디 투표 삭제 완료"),
    _STUDY_VOTE_UPDATED(HttpStatus.OK, "VOTE2004", "스터디 투표 수정 완료"),
    _STUDY_VOTE_PARTICIPATED(HttpStatus.CREATED, "VOTE2005", "스터디 투표 참여 완료"),
    _STUDY_VOTE_DETAIL_STATUS_FOUND(HttpStatus.OK, "VOTE2006", "스터디 투표 현황 조회 완료"),

    // 스터디 회원 신고 관련
    _STUDY_MEMBER_REPORTED(HttpStatus.CREATED, "REPORT2001", "스터디 회원 신고 완료"),
    _STUDY_POST_REPORTED(HttpStatus.CREATED, "REPORT2002", "스터디 게시글 신고 완료"),

    // 할 일 관련 응답
    _TO_DO_LIST_CREATED(HttpStatus.CREATED, "TODO2001", "TO-DO List 생성 완료"),
    _TO_DO_LIST_UPDATED(HttpStatus.OK, "TODO2002", "TO-DO List 상태 변경 완료"),
    _TO_DO_LIST_DELETED(HttpStatus.NO_CONTENT, "TODO2003", "TO-DO List 삭제 완료"),
    _TO_DO_LIST_FOUND(HttpStatus.OK, "TODO2004", "TO-DO List 조회 완료"),

    // 이미지 관련 응답
    _IMAGE_UPLOADED(HttpStatus.CREATED, "IMAGE2001", "이미지 업로드 완료"),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}

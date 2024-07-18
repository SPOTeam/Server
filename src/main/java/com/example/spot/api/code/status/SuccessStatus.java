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
    _MEMBER_CREATED(HttpStatus.CREATED, "MEMBER2001", "회원 가입 완료"),
    _MEMBER_UPDATED(HttpStatus.OK, "MEMBER2002", "회원 정보 수정 완료"),
    _MEMBER_DELETED(HttpStatus.OK, "MEMBER2003", "회원 탈퇴 완료"),
    _MEMBER_FOUND(HttpStatus.OK, "MEMBER2004", "회원 조회 완료"),
    _MEMBER_LIST_FOUND(HttpStatus.OK, "MEMBER2005", "회원 목록 조회 완료"),

    //스터디 게시물 관련 응답
    STUDY_POST_CREATED(HttpStatus.CREATED, "STUDYPOST3001", "스터디 게시글 작성 완료"),
    _STUDY_POST_UPDATED(HttpStatus.OK, "STUDYPOST3002", "스터디 게시글 수정 완료"),
    _STUDY_POST_DELETED(HttpStatus.OK, "STUDYPOST3003", "스터디 게시글 삭제 완료"),
    _STUDY_POST_FOUND(HttpStatus.OK, "STUDYPOST3004", "스터디 게시글 조회 완료"),
    _STUDY_POST_LIST_FOUND(HttpStatus.OK, "STUDYPOST3005", "스터디 게시글 목록 조회 완료"),
    _STUDY_POST_LIKED(HttpStatus.OK, "STUDYPOST3006", "스터디 게시글 좋아요 완료"),
    _STUDY_POST_UNLIKED(HttpStatus.OK, "STUDYPOST3007", "스터디 게시글 좋아요 취소 완료"),
    _STUDY_POST_COMMENT_CREATED(HttpStatus.CREATED, "STUDYPOST3008", "스터디 게시글 댓글 작성 완료"),
    _STUDY_POST_COMMENT_UPDATED(HttpStatus.OK, "STUDYPOST3009", "스터디 게시글 댓글 수정 완료"),
    _STUDY_POST_COMMENT_DELETED(HttpStatus.OK, "STUDYPOST3010", "스터디 게시글 댓글 삭제 완료"),
    _STUDY_FOUND(HttpStatus.OK, "STUDYPOST3011", "스터디 조회 완료");

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

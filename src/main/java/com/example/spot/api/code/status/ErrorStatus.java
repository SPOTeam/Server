package com.example.spot.api.code.status;

import com.example.spot.api.code.BaseErrorCode;
import com.example.spot.api.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    //공통 에러
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류 발생"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증되지 않은 요청입니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "접근이 거부되었습니다."),

    //멤버 관련 에러
    _MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "회원을 찾을 수 없습니다."),
    _MEMBER_NAME_INVALID(HttpStatus.BAD_REQUEST, "MEMBER4002", "회원 이름이 유효하지 않습니다."),
    _MEMBER_BIRTH_INVALID(HttpStatus.BAD_REQUEST, "MEMBER4005", "생년월일이 유효하지 않습니다."),

    //스터디 게시물 관련 에러
    _STUDY_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4001", "스터디 게시글을 찾을 수 없습니다."),
    _STUDY_POST_TITLE_INVALID(HttpStatus.BAD_REQUEST, "POST4002", "스터디 게시글 제목이 유효하지 않습니다."),
    _STUDY_POST_CONTENT_INVALID(HttpStatus.BAD_REQUEST, "POST4003", "스터디 게시글 내용이 유효하지 않습니다."),

    //알림 관련 에러
    _NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4001", "알림을 찾을 수 없습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build()
                ;
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}

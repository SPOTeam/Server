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
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON4000", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON4001", "인증되지 않은 요청입니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON4002", "접근이 거부되었습니다."),
    _BAD_VALUE_REQUEST(HttpStatus.BAD_REQUEST, "COMMON4003", "올바르지 않은 값을 입력 하셨습니다."),
    _INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON4004", "입력값이 유효하지 않습니다."),
    _EMPTY_JWT(HttpStatus.BAD_REQUEST, "COMMON4005", "JWT 토큰이 비어있습니다."),
    _INVALID_JWT(HttpStatus.BAD_REQUEST, "COMMON4006", "유효하지 않은 JWT token입니다."),
    _EXPIRED_JWT(HttpStatus.BAD_REQUEST, "COMMON4007", "만료된 JWT token입니다."),
    _UNSUPPORTED_JWT(HttpStatus.BAD_REQUEST, "COMMON4008", "지원되지 않는 JWT token입니다."),
    _INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "COMMON4009", "유효하지 않은 리프레시 토큰입니다."),
    _EXPIRED_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "COMMON4010", "만료된 리프레시 토큰입니다."),
    _NULL_VALUE(HttpStatus.BAD_REQUEST, "COMMON4011", "값이 입력되지 않았습니다."),
    _VALUE_RANGE_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMON4012", "값이 지정된 범위를 초과합니다."),

    //멤버 관련 에러
    _MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "회원을 찾을 수 없습니다."),
    _MEMBER_NAME_INVALID(HttpStatus.BAD_REQUEST, "MEMBER4002", "회원 이름이 유효하지 않습니다."),
    _MEMBER_ID_NULL(HttpStatus.BAD_REQUEST, "MEMBER4003", "회원 아이디가 입력되지 않았습니다. "),
    _MEMBER_BIRTH_INVALID(HttpStatus.BAD_REQUEST, "MEMBER4005", "생년월일이 유효하지 않습니다."),
    _MEMBER_EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER4006", "이미 존재하는 이메일입니다."),
    _MEMBER_NO_ACCESS(HttpStatus.FORBIDDEN, "MEMBER4007", "해당 API에 대한 접근 권한이 없습니다."),
    _INVALID_STUDY_REASON(HttpStatus.BAD_REQUEST, "MEMBER4008", "유효하지 않은 스터디 이유입니다."),
    _MEMBER_THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4009", "해당하는 회원의 관심 테마를 찾을 수 없습니다."),
    _MEMBER_REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4010", "해당하는 회원의 관심 지역을 찾을 수 없습니다."),
    _MEMBER_STUDY_REASON_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4011", "해당하는 회원의 스터디 이유를 찾을 수 없습니다."),

    //스터디 관련 에러
    _STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY4001", "스터디를 찾을 수 없습니다."),
    _STUDY_OWNER_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY4002", "스터디장을 찾을 수 없습니다."),
    _STUDY_ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "STUDY4003", "이미 신청된 스터디입니다."),
    _STUDY_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY4004", "스터디 회원을 찾을 수 없습니다."),
    _STUDY_NOT_APPROVED(HttpStatus.FORBIDDEN, "STUDY4005", "승인되지 않은 스터디입니다."),
    _STUDY_OWNER_CANNOT_WITHDRAW(HttpStatus.FORBIDDEN, "STUDY4006", "스터디장은 스터디를 탈퇴할 수 없습니다."),
    _STUDY_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "STUDY4007", "스터디 모집기한이 아닙니다."),
    _STUDY_APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY4009", "처리를 기다리는 스터디 신청을 찾을 수 없습니다."),
    _STUDY_APPLY_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "STUDY4010","스터디 신청이 이미 처리된 회원입니다."),
    _STUDY_OWNER_CANNOT_APPLY(HttpStatus.BAD_REQUEST, "STUDY4011", "스터디장은 스터디에 신청할 수 없습니다."),
    _STUDY_IS_FULL(HttpStatus.BAD_REQUEST, "STUDY4012", "스터디 인원이 가득 찼습니다."),
    _ONLY_STUDY_OWNER_CAN_ACCESS_APPLICANTS(HttpStatus.FORBIDDEN, "STUDY4013", "스터디장만 신청자 목록에 접근할 수 있습니다."),
    _ONLY_STUDY_MEMBER_CAN_ACCESS_ANNOUNCEMENT_POST(HttpStatus.FORBIDDEN, "STUDY4014", "스터디 멤버만 공지 게시글에 접근할 수 있습니다."),
    _ONLY_STUDY_MEMBER_CAN_ACCESS_SCHEDULE(HttpStatus.FORBIDDEN, "STUDY4015", "스터디 멤버만 일정에 접근할 수 있습니다."),
    _ONLY_STUDY_MEMBER_CAN_ACCESS_MEMBERS(HttpStatus.FORBIDDEN, "STUDY4016", "스터디 멤버만 회원 목록에 접근할 수 있습니다."),
    _ALREADY_STUDY_MEMBER(HttpStatus.BAD_REQUEST, "STUDY4017", "이미 스터디 멤버입니다."),

    //스터디 게시글 관련 에러
    _STUDY_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4001", "스터디 게시글을 찾을 수 없습니다."),
    _STUDY_POST_TITLE_INVALID(HttpStatus.BAD_REQUEST, "POST4002", "스터디 게시글 제목이 유효하지 않습니다."),
    _STUDY_POST_CONTENT_INVALID(HttpStatus.BAD_REQUEST, "POST4003", "스터디 게시글 내용이 유효하지 않습니다."),
    _STUDY_POST_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "POST4004", "이미 좋아요 한 게시글입니다."),
    _STUDY_LIKED_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4005", "좋아요를 누르지 않은 게시글의 좋아요를 취소할 수 없습니다."),
    _STUDY_POST_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4006", "스터디 게시글의 댓글을 찾을 수 없습니다."),
    _STUDY_POST_ANNOUNCEMENT_INVALID(HttpStatus.BAD_REQUEST, "POST4007", "일반 스터디원에게는 공지 권한이 없습니다."),
    _STUDY_POST_COMMENT_DELETE_INVALID(HttpStatus.FORBIDDEN, "POST4008", "댓글은 작성자만 삭제할 수 있습니다."),
    _STUDY_POST_COMMENT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "POST4009", "이미 삭제된 댓글입니다."),
    _STUDY_POST_COMMENT_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "POST4010", "이미 좋아요 한 댓글입니다."),
    _STUDY_POST_COMMENT_ALREADY_DISLIKED(HttpStatus.BAD_REQUEST, "POST4011", "이미 싫어요 한 댓글입니다."),
    _STUDY_LIKED_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4012", "좋아요를 누르지 않은 게시글의 좋아요를 취소할 수 없습니다."),
    _STUDY_DISLIKED_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4013", "싫어요를 누르지 않은 게시글의 싫어요를 취소할 수 없습니다."),
    _STUDY_POST_DELETION_INVALID(HttpStatus.FORBIDDEN, "POST4014", "게시글 작성자만 삭제 가능합니다."),
    _STUDY_POST_NULL(HttpStatus.BAD_REQUEST, "POST4015", "게시글 아이디가 입력되지 않았습니다."),
    _STUDY_POST_COMMENT_NULL(HttpStatus.BAD_REQUEST, "POST4016", "댓글 아이디가 입력되지 않았습니다."),
    _STUDY_POST_COMMENT_REACTIOM_ID_NULL(HttpStatus.BAD_REQUEST, "POST4017", "댓글 반응 아이디가 입력되지 않았습니다."),
    _STUDY_POST_COMMENT_REACTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "POST4018", "댓글 반응이 존재하지 않습니다."),

    //스터디 일정 관련 에러
    _STUDY_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE4001", "스터디 일정을 찾을 수 없습니다."),
    _STUDY_SCHEDULE_ID_NULL(HttpStatus.BAD_REQUEST, "SCHEDULE4002", "일정 아이디가 입력되지 않았습니다."),
    _SCHEDULE_MOD_INVALID(HttpStatus.FORBIDDEN, "SCHEDULE4003", "일정을 생성한 회원만 수정 가능합니다."),

    //알림 관련 에러
    _NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4001", "알림을 찾을 수 없습니다."),
    _NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "NOTIFICATION4002", "이미 처리된 알림입니다."),
    _NOTIFICATION_NULL(HttpStatus.BAD_REQUEST, "NOTIFICATION4003", "알림 아이디가 입력되지 않았습니다."),
    _NOTIFICATION_IS_NOT_BELONG_TO_MEMBER(HttpStatus.BAD_REQUEST, "NOTIFICATION4004", "해당 알림이 해당 회원에 속해있지 않습니다."),

    //지역 관련 에러
    _REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGION4001", "지역을 찾을 수 없습니다."),

    // 스터디 테마 에러
    _THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "THEME4001", "스터디 테마를 찾을 수 없습니다."),
    _STUDY_THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY4002", "스터디 관심사를 찾을 수 없습니다."),
    _STUDY_REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY4003", "스터디 지역을 찾을 수 없습니다."),

    // 스터디 조회 관련 에러
    _STUDY_THEME_IS_NULL(HttpStatus.BAD_REQUEST, "STUDY6004", "스터디 관심사가 입력되지 않았습니다."),
    _STUDY_REGION_IS_NULL(HttpStatus.BAD_REQUEST, "STUDY6005", "스터디 지역이 입력되지 않았습니다."),
    _STUDY_SORT_BY_IS_NULL(HttpStatus.BAD_REQUEST, "STUDY6006", "스터디 정렬 기준이 입력되지 않았습니다."),
    _STUDY_SORT_BY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY6007", "스터디 정렬 기준을 찾을 수 없습니다."),
    _STUDY_ID_NULL(HttpStatus.BAD_REQUEST, "STUDY6008", "스터디 아이디가 입력되지 않았습니다."),
    _STUDY_REGION_IS_NOT_MATCH(HttpStatus.BAD_REQUEST, "STUDY6009", "입력한 회원의 관심 스터디 지역을 입력하세요."),
    _STUDY_IS_NOT_MATCH(HttpStatus.BAD_REQUEST, "STUDY6010", "입력한 조건에 맞는 스터디가 존재하지 않습니다."),
    _STUDY_THEME_IS_INVALID(HttpStatus.BAD_REQUEST, "STUDY6011", "해당 회원의 유효한 스터디 관심사가 존재하지 않습니다."),

    // 스터디 출석 관련 에러
    _STUDY_QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "QUIZ4001", "출석 퀴즈를 찾을 수 없습니다."),
    _STUDY_QUIZ_NOT_VALID(HttpStatus.BAD_REQUEST, "QUIZ4002", "출석 퀴즈의 제한 시간이 초과되었습니다."),
    _STUDY_QUIZ_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "QUIZ4003", "금일 출석 퀴즈가 이미 존재합니다."),
    _STUDY_QUIZ_ID_NULL(HttpStatus.BAD_REQUEST, "QUIZ4004", "출석 퀴즈 아이디가 입력되지 않았습니다."),
    _STUDY_QUIZ_CREATION_INVALID(HttpStatus.FORBIDDEN, "QUIZ4005", "출석 퀴즈는 스터디장만 생성할 수 있습니다."),
    _STUDY_ATTENDANCE_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "QUIZ4004", "이미 출석 체크되었습니다."),
    _STUDY_ATTENDANCE_ATTEMPT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "QUIZ4005", "출석 퀴즈 시도 횟수가 초과되었습니다."),

    // S3 관련 에러
    _FILE_IS_NULL(HttpStatus.BAD_REQUEST, "S34001", "파일이 입력되지 않았습니다."),
    _IO_EXCEPTION(HttpStatus.BAD_REQUEST, "S34002", "IO 오류가 발생했습니다."),
    _BAD_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "S34003", "잘못된 확장자입니다."),
    _PUT_OBJECT_EXCEPTION(HttpStatus.BAD_REQUEST, "S34004", "이미지 업로드하는 과정에서 오류가 발생했습니다."),

    //게시글 관련 에러
    _POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST5001", "게시글을 찾을 수 없습니다."),
    _POST_TITLE_INVALID(HttpStatus.BAD_REQUEST, "POST5002", "게시글 제목이 유효하지 않습니다."),
    _POST_CONTENT_INVALID(HttpStatus.BAD_REQUEST, "POST5003", "게시글 내용이 유효하지 않습니다."),
    _POST_NOT_AUTHOR(HttpStatus.BAD_REQUEST, "POST5004", "게시글 작성자가 아닙니다."),
    _POST_REPORTED(HttpStatus.FORBIDDEN, "POST5005", "신고된 게시글입니다."),
    _INVALID_BOARD_TYPE(HttpStatus.BAD_REQUEST, "POST5006", "유효하지 않은 게시판 타입입니다."),
    _INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "POST5007", "유효하지 않은 인기글 타입입니다."),
    _POST_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "POST5008", "이미 좋아요한 게시글입니다."),
    _POST_NOT_LIKED(HttpStatus.BAD_REQUEST, "POST5009", "좋아요하지 않은 게시글입니다."),
    _POST_ID_NULL(HttpStatus.BAD_REQUEST, "POST5010", "게시글 아이디가 입력되지 않았습니다."),
    _POST_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POST5011", "게시글 댓글을 찾을 수 없습니다."),
    _POST_COMMENT_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "POST5012", "이미 좋아요한 댓글입니다."),
    _POST_COMMENT_NOT_LIKED(HttpStatus.BAD_REQUEST, "POST5013", "좋아요하지 않은 게시글 댓글입니다."),
    _POST_COMMENT_ALREADY_DISLIKED(HttpStatus.BAD_REQUEST, "POST5014", "이미 싫어요한 댓글입니다."),
    _POST_COMMENT_NOT_DISLIKED(HttpStatus.BAD_REQUEST, "POST5015", "싫어요하지 않은 게시글 댓글입니다."),
    _POST_ALREADY_SCRAPPED(HttpStatus.BAD_REQUEST, "POST5016", "이미 스크랩한 게시글입니다."),
    _POST_NOT_SCRAPPED(HttpStatus.BAD_REQUEST, "POST5017", "스크랩하지 않은 게시글입니다."),

    // 스터디 투표 관련 에러
    _STUDY_VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE4001", "스터디 투표를 찾을 수 없습니다."),
    _STUDY_VOTE_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE4002", "스터디 투표 항목을 찾을 수 없습니다."),
    _STUDY_VOTE_MULTIPLE_CHOICE_NOT_VALID(HttpStatus.BAD_REQUEST, "VOTE4003", "중복 선택이 불가능한 투표입니다."),
    _STUDY_VOTE_RE_PARTICIPATION_INVALID(HttpStatus.BAD_REQUEST, "VOTE4004", "이미 참여한 투표입니다."),
    _STUDY_VOTE_CREATOR_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "VOTE4005", "투표를 생성한 회원만 투표를 편집할 수 있습니다."),
    _STUDY_VOTE_IS_IN_PROGRESS(HttpStatus.BAD_REQUEST, "VOTE4006", "진행중인 투표는 편집할 수 없습니다."),
    _STUDY_VOTE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "VOTE4007", "마감되지 않은 스터디 투표입니다."),
    _STUDY_VOTE_NULL(HttpStatus.BAD_REQUEST, "VOTE4008", "스터디 투표 아이디가 입력되지 않았습니다."),

    // 신고 기능 관련 에러
    _STUDY_MEMBER_REPORT_INVALID(HttpStatus.BAD_REQUEST, "REPORT4001", "자기 자신을 신고할 수 없습니다."),

    // 스터디 투두 리스트 관련 에러
    _STUDY_TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO4001", "스터디 투두 리스트를 찾을 수 없습니다."),
    _STUDY_TODO_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "TODO4002", "해당 투두 리스트를 수정할 권한이 없습니다."),
    _STUDY_TODO_IS_NOT_BELONG_TO_STUDY(HttpStatus.BAD_REQUEST, "TODO4003", "해당 투두 리스트가 해당 스터디에 속해있지 않습니다."),
    _ONLY_STUDY_MEMBER_CAN_ACCESS_TODO_LIST(HttpStatus.FORBIDDEN, "TODO4004", "스터디 멤버만 투두 리스트에 접근할 수 있습니다."),
    _TODO_LIST_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE4004", "일정을 조회하려는 멤버가 스터디에 가입되지 않았습니다."),
    _STUDY_TODO_NULL(HttpStatus.BAD_REQUEST, "TODO4005", "투두 리스트 아이디가 입력되지 않았습니다."),;

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

package com.example.spot.api.exception;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.ErrorStatus;
import io.sentry.Sentry;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    /**
     * GeneralException 처리
     * @param exception GeneralException
     * @return ApiResponse - GeneralException
     */
    @ExceptionHandler(GeneralException.class)
    public ApiResponse<ErrorStatus> baseExceptionHandle(GeneralException exception) {
        log.warn("BaseException. error message: {}", exception.getMessage());
        return new ApiResponse<>(exception.getStatus());
    }

    /**
     * Exception 처리
     * @param exception Exception
     * @return ApiResponse - INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<ErrorStatus> exceptionHandle(Exception exception) {
        log.error("Exception has occurred. {}", exception);
        return new ApiResponse<>(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    /**
     * MethodArgumentTypeMismatchException 처리 - 잘못된 값 입력
     * @param ex MethodArgumentTypeMismatchException
     * @return ApiResponse - BAD_VALUE_REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("올바르지 않은 값을 입력 하셨습니다. %s: %s", ex.getName(), ex.getValue());
        log.warn("MethodArgumentTypeMismatchException. error message: {}", errorMessage);
        return new ApiResponse<>(ErrorStatus._BAD_VALUE_REQUEST, errorMessage);
    }

    /**
     * ConstraintViolationException 처리
     * @param exception ConstraintViolationException 객체
     * @return 클라이언트에게 반환할 ApiResponse 객체
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleConstraintViolationException(ConstraintViolationException exception) {
        // 모든 필드 오류 메시지를 수집
        List<String> errors = exception.getConstraintViolations()
            .stream()
            // 각 ConstraintViolation에서 필드 경로와 메시지를 포맷하여 수집
            .map(constraintViolation -> String.format("'%s': %s ", constraintViolation.getPropertyPath(), constraintViolation.getMessage()))
            .collect(Collectors.toList());

        // 모든 에러 메시지를 하나의 문자열로 결합
        String errorMessage = String.join(", ", errors);
        // 경고 로그로 에러 메시지를 출력
        log.warn("ConstraintViolationException. error message: {}", errorMessage);

        // ApiResponse 객체를 생성하여 오류 정보를 포함
        ApiResponse<List<String>> response = ApiResponse.onFailure(
            ErrorStatus._BAD_REQUEST.getCode(), // HTTP 상태 코드
            ErrorStatus._BAD_REQUEST.getMessage(), // 오류 메시지
            errors // 오류 목록
        );

        // BAD_REQUEST 상태와 함께 ApiResponse 반환
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    /**
     * MethodArgumentNotValidException 처리 - @Valid 유효성 검사 실패
     * @param exception MethodArgumentNotValidException 객체
     * @return 클라이언트에게 반환할 ApiResponse 객체
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        // 모든 필드 오류 메시지를 수집
        List<String> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> String.format("'%s': %s ", fieldError.getField(), fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

        // 모든 에러 메시지를 하나의 문자열로 결합
        String errorMessage = String.join(", ", errors);
        log.warn("MethodArgumentNotValidException. error message: {}", errorMessage);

        ApiResponse<List<String>> response = ApiResponse.onFailure(
            ErrorStatus._BAD_REQUEST.getCode(),
            ErrorStatus._BAD_REQUEST.getMessage(),
            errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Sentry에 예외를 캡처합니다.
    private void captureException(Exception exception) {
        if (Sentry.isEnabled()) {
            Sentry.captureException(exception);
        }
    }

}
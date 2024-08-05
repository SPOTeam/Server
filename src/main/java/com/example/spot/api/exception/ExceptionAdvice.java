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

    @ExceptionHandler(GeneralException.class)
    public ApiResponse<ErrorStatus> baseExceptionHandle(GeneralException exception) {
        log.warn("BaseException. error message: {}", exception.getMessage());
        return new ApiResponse<>(exception.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<ErrorStatus> exceptionHandle(Exception exception) {
        log.error("Exception has occurred. {}", exception);
        return new ApiResponse<>(ErrorStatus._INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("잘못된 enum 값을 입력 하셨습니다. %s: %s", ex.getName(), ex.getValue());
        log.warn("MethodArgumentTypeMismatchException. error message: {}", errorMessage);
        return new ApiResponse<>(ErrorStatus._BAD_ENUM_REQUEST, errorMessage);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleConstraintViolationException(ConstraintViolationException exception) {
        // 모든 필드 오류 메시지를 수집
        List<String> errors = exception.getConstraintViolations()
            .stream()
            .map(constraintViolation -> String.format("'%s': %s ", constraintViolation.getPropertyPath(), constraintViolation.getMessage()))
            .collect(Collectors.toList());

        // 모든 에러 메시지를 하나의 문자열로 결합
        String errorMessage = String.join(", ", errors);
        log.warn("ConstraintViolationException. error message: {}", errorMessage);

        ApiResponse<List<String>> response = ApiResponse.onFailure(
            ErrorStatus._BAD_REQUEST.getCode(),
            ErrorStatus._BAD_REQUEST.getMessage(),
            errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

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
    private void captureException(Exception exception) {
        if (Sentry.isEnabled()) {
            Sentry.captureException(exception);
        }
    }

}
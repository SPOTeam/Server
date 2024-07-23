package com.example.spot.api.exception;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.ErrorReasonDTO;
import com.example.spot.api.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {

        String errorMessage = e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 실패"));

        return handleExceptionInternalConstraint(e, ErrorStatus.valueOf(errorMessage), HttpHeaders.EMPTY, request);
    }

    //@Override
    //@Nullable
    public ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
            .forEach(fieldError -> {
                String fieldName = fieldError.getField();
                String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                errors.merge(fieldName, errorMessage, (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
            });

        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, ErrorStatus.valueOf("_BAD_REQUEST"), request, errors);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {

        e.printStackTrace();
        return handleExceptionInternalFalse(e, ErrorStatus._INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(), request, e.getMessage());
    }

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity onThrowException(GeneralException generalException, HttpServletRequest request) {

        ErrorReasonDTO errorReasonDTOHttpStatus = generalException.getErrorReasonHttpStatus();
        return handleExceptionInternal(generalException, errorReasonDTOHttpStatus, null, request);
    }

    private ResponseEntity<Object> handleExceptionInternal(
        Exception e, ErrorReasonDTO reason, HttpHeaders headers, HttpServletRequest request) {

        ApiResponse<Object> body = ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null);
        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
            e,
            body,
            headers,
            reason.getHttpStatus(),
            webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(
        Exception e, ErrorStatus errorCommonStatus, HttpHeaders headers, HttpStatus status, WebRequest request, String errorPoint) {

        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), errorPoint);
        return super.handleExceptionInternal(
            e,
            body,
            headers,
            status,
            request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(
        Exception e, HttpHeaders headers, ErrorStatus errorCommonStatus, WebRequest request, Map<String, String> errorArgs) {

        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), errorArgs);
        return super.handleExceptionInternal(
            e,
            body,
            headers,
            errorCommonStatus.getHttpStatus(),
            request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(
        Exception e, ErrorStatus errorCommonStatus, HttpHeaders headers, WebRequest request) {

        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), null);
        return super.handleExceptionInternal(
            e,
            body,
            headers,
            errorCommonStatus.getHttpStatus(),
            request
        );
    }
}
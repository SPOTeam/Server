package com.example.spot.api;

import com.example.spot.api.code.BaseCode;
import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.code.status.SuccessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private final boolean isSuccess;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;


    @JsonProperty("isSuccess")
    public boolean isSuccess() {
        return isSuccess;
    }

    // 성공한 경우 응답 생성
    public static <T> ApiResponse<T> onSuccess(SuccessStatus code, T result) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), result);

    }

    public static <T> ApiResponse<T> of(BaseCode code, T result) {
        return new ApiResponse<>(true, code.getReasonHttpStatus().getCode(), code.getReasonHttpStatus().getMessage(), result);
    }

    // 성공한 경우 응답 생성 (Void 타입 지원)
    public static ApiResponse<Void> onSuccess(SuccessStatus code) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), null);
    }

    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

    public ApiResponse(ErrorStatus status) {
        this.isSuccess = false;
        this.message = status.getMessage();
        this.code = status.getCode();
    }

    public ApiResponse(ErrorStatus status, T result) {
        this.isSuccess = false;
        this.message = status.getMessage();
        this.code = status.getCode();
        this.result = result;
    }
}

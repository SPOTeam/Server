package com.example.spot.api.exception;

import com.example.spot.api.code.status.ErrorStatus;
import io.sentry.Sentry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralException extends RuntimeException {
    private ErrorStatus status;
    public GeneralException(ErrorStatus status) {
        super(status.getCode());
        this.status = status;

        // 500번대 에러인 경우, Sentry에 에러 로그를 기록한다.
        if (status.getCode().equals(ErrorStatus._INTERNAL_SERVER_ERROR.getCode()))
            Sentry.captureException(this);
    }
}

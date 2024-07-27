package com.example.spot.api.exception;

import com.example.spot.api.code.status.ErrorStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralException extends RuntimeException {
    private ErrorStatus status;
    public GeneralException(ErrorStatus status) {
        super(status.getCode());
        this.status = status;
    }
}

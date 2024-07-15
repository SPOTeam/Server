package com.example.spot.api.code.status;

import com.example.spot.api.code.BaseErrorCode;
import com.example.spot.api.code.ErrorReason;

public enum ErrorStatus implements BaseErrorCode {
    ;

    @Override
    public ErrorReason getReason() {
        return null;
    }

    @Override
    public ErrorReason getReasonHttpStatus() {
        return null;
    }
}

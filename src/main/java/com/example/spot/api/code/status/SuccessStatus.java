package com.example.spot.api.code.status;

import com.example.spot.api.code.BaseCode;
import com.example.spot.api.code.Reason;

public enum SuccessStatus implements BaseCode {
    ;

    @Override
    public Reason getReason() {
        return null;
    }

    @Override
    public Reason getReasonHttpStatus() {
        return null;
    }
}

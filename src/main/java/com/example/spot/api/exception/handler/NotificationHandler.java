package com.example.spot.api.exception.handler;

import com.example.spot.api.code.BaseErrorCode;
import com.example.spot.api.exception.GeneralException;

public class NotificationHandler extends GeneralException {

    public NotificationHandler(BaseErrorCode code) {
        super(code);
    }
}

package com.example.spot.api.exception.handler;

import com.example.spot.api.code.BaseErrorCode;
import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;

public class PostHandler extends GeneralException {

    public PostHandler(ErrorStatus code) {
        super(code);
    }
}

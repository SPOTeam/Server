package com.example.spot.api.exception.handler;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;

public class S3Handler extends GeneralException {

    public S3Handler(ErrorStatus code) {
        super(code);
    }
}
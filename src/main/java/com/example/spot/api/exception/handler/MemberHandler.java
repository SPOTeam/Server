package com.example.spot.api.exception.handler;

import com.example.spot.api.code.BaseErrorCode;
import com.example.spot.api.exception.GeneralException;

public class MemberHandler extends GeneralException {

    public MemberHandler(BaseErrorCode code) {
        super(code);
    }
}

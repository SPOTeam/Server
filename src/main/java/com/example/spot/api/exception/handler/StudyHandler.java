package com.example.spot.api.exception.handler;

import com.example.spot.api.code.BaseErrorCode;
import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;

public class StudyHandler extends GeneralException {

    public StudyHandler(ErrorStatus status){
        super(status);
    }


}

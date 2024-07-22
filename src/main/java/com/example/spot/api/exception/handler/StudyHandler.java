package com.example.spot.api.exception.handler;

import com.example.spot.api.code.BaseErrorCode;
import com.example.spot.api.exception.GeneralException;

public class StudyHandler extends GeneralException {

    public StudyHandler(BaseErrorCode baseErrorCode){
        super(baseErrorCode);
    }


}

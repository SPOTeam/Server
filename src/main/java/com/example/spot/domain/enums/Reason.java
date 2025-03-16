package com.example.spot.domain.enums;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;

public enum Reason {
    꾸준한_학습_습관이_필요해요(1),
    상호_피드백이_필요해요(2),
    네트워킹을_하고_싶어요(3),
    자격증을_취득하고_싶어요(4),
    대회에_참가하여_수상하고_싶어요(5),
    다양한_의견을_나누고_싶어요(6);

    private final long code;

    Reason(long code) {
        this.code = code;
    }

    public long getCode() {
        return code;
    }

    public static Reason fromCode(long code) {
        for (Reason reason : values()) {
            if (reason.getCode() == code) {
                return reason;
            }
        }
        throw new GeneralException(ErrorStatus._INVALID_STUDY_REASON);
    }
}

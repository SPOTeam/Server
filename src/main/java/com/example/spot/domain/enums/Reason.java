package com.example.spot.domain.enums;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;

public enum Reason {
    동기부여를_받고_싶어요(1),
    스터디원이_필요해요(2),
    혼자서_하기가_의지가_부족해요(3),
    한_목표를_가진_사람들과_친해지고_싶어요(4),
    다양한_정보를_공유하고_받고_싶어요(5);

    private final int code;

    Reason(int code) {
        this.code = code;
    }

    public long getCode() {
        return code;
    }

    public static Reason fromCode(int code) {
        for (Reason reason : values()) {
            if (reason.getCode() == code) {
                return reason;
            }
        }
        throw new GeneralException(ErrorStatus._INVALID_STUDY_REASON);
    }
}

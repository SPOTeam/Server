package com.example.spot.domain.enums;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.StudyHandler;

public enum ThemeQuery {

    ANNOUNCEMENT, WELCOME, INFO_SHARING, STUDY_REVIEW, FREE_TALK, QNA

    ;

    public Theme toTheme() {
        return switch (this) {
            case WELCOME -> Theme.WELCOME;
            case INFO_SHARING -> Theme.INFO_SHARING;
            case STUDY_REVIEW -> Theme.STUDY_REVIEW;
            case FREE_TALK -> Theme.FREE_TALK;
            case QNA -> Theme.QNA;
            default -> throw new StudyHandler(ErrorStatus._THEME_NOT_FOUND);
        };
    }
}

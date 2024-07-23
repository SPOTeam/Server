package com.example.spot.web.dto.study.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class StudyJoinRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyJoinDTO {
        private String introduction;
    }
}

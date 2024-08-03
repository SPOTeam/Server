package com.example.spot.web.dto.study.request;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.validation.annotation.IntSize;
import com.example.spot.validation.annotation.TextLength;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class StudyRegisterRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterDTO {

        private List<ThemeType> themes;

        @TextLength(min = 1, max = 255)
        private String title;

        @TextLength(min = 1, max = 255)
        private String goal;

        @TextLength(min = 1, max = 255)
        private String introduction;

        private Boolean isOnline;

        @TextLength(min = 1, max = 255)
        private String profileImage;

        private List<String> regions;

        @IntSize(min = 2)
        private Long maxPeople;

        private Gender gender;

        @IntSize(min = 1)
        private Integer minAge;

        @IntSize(min = 1)
        private Integer maxAge;

        @IntSize(min = 0)
        private Integer fee;

        private boolean hasFee;
    }

}

package com.example.spot.web.dto.study.request;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.ThemeType;
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

        private Gender gender;
        private Integer minAge;
        private Integer maxAge;
        private Integer fee;
        private String profileImage;
        private Boolean isOnline;
        private boolean hasFee;
        private String goal;
        private String introduction;
        private String title;
        private Long maxPeople;
        private List<RegionDTO> regions;
        private List<ThemeType> themes;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionDTO {

        private String province;
        private String district;
        private String neighborhood;
    }
}

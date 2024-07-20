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

        private List<ThemeType> themes;
        private String title;
        private String goal;
        private String introduction;
        private Boolean isOnline;
        private List<RegionDTO> regions;
        private Long maxPeople;
        private Gender gender;
        private Integer minAge;
        private Integer maxAge;
        private Integer fee;
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

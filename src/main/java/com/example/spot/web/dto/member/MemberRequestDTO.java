package com.example.spot.web.dto.member;

import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.enums.ThemeType;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MemberRequestDTO {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberCheckListDTO{
        private List<ThemeType> themes;
        private List<String> regionCodes;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberInfoListDTO{
        private String name;
        private LocalDate birth;
        private Carrier carrier;
        private String phone;
        private boolean idInfo;
        private boolean personalInfo;
    }



}

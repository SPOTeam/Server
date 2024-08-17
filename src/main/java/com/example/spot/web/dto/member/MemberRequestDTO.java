package com.example.spot.web.dto.member;

import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.enums.ThemeType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
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
    public static class MemberThemeDTO{
        private List<ThemeType> themes;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberRegionDTO{
        private List<String> regions;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberReasonDTO{
        private List<Integer> reasons;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberInfoListDTO{

        private String name;
        private Gender gender;
        private String email;
        private LocalDate birth;
        private Carrier carrier;
        private String profileImage;
        private String phone;
        private boolean idInfo;
        private boolean personalInfo;

        private MemberRegionDTO regions;
        private MemberThemeDTO themes;

        @AssertTrue(message = "휴대폰 번호는 11자리 이하로 입력해주세요.")
        private boolean isPhoneNumValid(){
            return phone.length() <= 11;
        }
    }

}

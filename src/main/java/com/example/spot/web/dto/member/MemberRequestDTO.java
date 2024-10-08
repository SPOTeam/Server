package com.example.spot.web.dto.member;

import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.validation.annotation.TextLength;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.util.List;

import lombok.*;

public class MemberRequestDTO {

    @Getter
    @RequiredArgsConstructor
    public static class SignInDTO {

        @TextLength(max = 50)
        private final String email;

        @TextLength(max = 100)
        private final String password;
    }

    @Getter
    @RequiredArgsConstructor
    public static class SignUpDTO {

        @TextLength(max = 20)
        private final String name;

        @TextLength(max = 8)
        private final String nickname;

        private final LocalDate birth;

        @TextLength(max = 50)
        private final String email;

        @TextLength(min=6, max = 100)
        private final String loginId;

        @TextLength(max = 100)
        private final String password;

        @TextLength(max = 100)
        private final String pwCheck;

        private final Boolean personalInfo;

        private final Boolean idInfo;
    }

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
        private String nickname;
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

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberUpdateDTO {

        private String name;
        private Gender gender;
        private String email;
        private LocalDate birth;
        private Carrier carrier;
        private String profileImage;
        private String phone;
        private boolean idInfo;
        private boolean personalInfo;

        @AssertTrue(message = "휴대폰 번호는 11자리 이하로 입력해주세요.")
        private boolean isPhoneNumValid() {
            return phone.length() <= 11;
        }
    }

}

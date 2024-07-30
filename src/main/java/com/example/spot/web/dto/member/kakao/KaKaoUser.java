package com.example.spot.web.dto.member.kakao;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.LoginType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class KaKaoUser {
    Long id;
    String connected_at;
    KaKaoPropertiesDTO properties;
    KaKaoAccountDTO kakao_account;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KaKaoAccountDTO {
        Boolean has_email;
        Boolean email_needs_agreement;
        Boolean is_email_valid;
        Boolean is_email_verified;
        String email;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KaKaoPropertiesDTO{
        String nickname;
        String profile_image;
        String thumbnail_image;
    }

    public Member toMember(){
        return Member.builder()
            .name(properties.getNickname())
            .email(kakao_account.getEmail())
            .profileImage(properties.getProfile_image())
            .carrier(Carrier.NONE)
            .phone("NONE")
            .birth(LocalDate.now())
            .personalInfo(false)
            .idInfo(false)
            .isAdmin(false)
            .loginType(LoginType.KAKAO)
            .build();
    }

}

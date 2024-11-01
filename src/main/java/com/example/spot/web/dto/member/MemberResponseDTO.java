package com.example.spot.web.dto.member;

import com.example.spot.domain.enums.LoginType;
import com.example.spot.domain.enums.Reason;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.Member;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import lombok.*;

import java.util.List;
import java.time.LocalDateTime;

@Getter
public class MemberResponseDTO {

    @Getter
    @RequiredArgsConstructor
    public static class AvailabilityDTO {
        private final boolean isAvailable;
        private final String reason;

        public static AvailabilityDTO toDTO(boolean isAvailable, String reason) {
            return new AvailabilityDTO(isAvailable, reason);
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class FindPwDTO {

        private final String nickname;
        private final String loginId;
        private final String tempPw;

        public static FindPwDTO toDTO(Member member) {
            return FindPwDTO.builder()
                    .nickname(member.getNickname())
                    .loginId(member.getLoginId())
                    .tempPw(member.getPassword())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class FindIdDTO {

        private final String account;
        private final LoginType loginType;
        private final LocalDateTime createdAt;

        public static FindIdDTO toDTO(Member member) {
            return FindIdDTO.builder()
                    .account(member.getLoginType().equals(LoginType.NORMAL) ?
                                member.getLoginId() : member.getEmail())
                    .loginType(member.getLoginType())
                    .createdAt(member.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class NaverSignInDTO {

        private final Boolean isSpotMember;
        private final MemberSignInDTO signInDTO;

        public static NaverSignInDTO toDTO(Boolean isSpotMember, MemberSignInDTO signInDTO) {
            return NaverSignInDTO.builder()
                    .isSpotMember(isSpotMember)
                    .signInDTO(signInDTO)
                    .build();
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSignInDTO{
        private TokenDTO tokens;
        private String email;
        private Long memberId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberTestDTO{
        private Long memberId;
        private String email;
        private TokenDTO tokens;

    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberUpdateDTO {
        private Long memberId;
        private LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberThemeDTO {
        private Long memberId;
        private List<ThemeType> themes;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberRegionDTO {
        private Long memberId;
        private List<RegionDTO> regions;


        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RegionDTO {
            private String province;
            private String district;
            private String neighborhood;
            private String code;
        }
    }



    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberStudyReasonDTO {
        private Long memberId;
        private List<Reason> reasons;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class ReportedMemberDTO {

        private final Long memberId;
        private final String name;

        public static ReportedMemberDTO toDTO(Member member) {
            return ReportedMemberDTO.builder()
                    .memberId(member.getId())
                    .name(member.getName())
                    .build();
        }
    }
}


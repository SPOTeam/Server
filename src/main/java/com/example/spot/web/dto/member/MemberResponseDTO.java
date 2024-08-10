package com.example.spot.web.dto.member;

import com.example.spot.domain.enums.Reason;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.Member;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import lombok.*;

import java.util.List;
import java.time.LocalDateTime;

@Getter
public class MemberResponseDTO {

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


package com.example.spot.web.dto.member;

import com.example.spot.domain.enums.Reason;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.ThemeType;
import java.time.LocalDateTime;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        private List<String> regions;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberStudyReasonDTO {
        private Long memberId;
        private List<Reason> reasons;
    }
}


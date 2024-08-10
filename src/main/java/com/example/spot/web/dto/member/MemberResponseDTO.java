package com.example.spot.web.dto.member;

import java.time.LocalDateTime;

import com.example.spot.domain.Member;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import lombok.*;

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


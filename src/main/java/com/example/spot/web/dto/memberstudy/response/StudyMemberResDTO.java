package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class StudyMemberResDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class StudyHostDTO {
        private final Boolean isOwned;
        private final HostInfoDTO host;
        public static StudyHostDTO toDTO(Boolean isOwned, Member host) {
            return StudyHostDTO.builder()
                    .isOwned(isOwned)
                    .host(HostInfoDTO.toDTO(host))
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    private static class HostInfoDTO {
        private final Long memberId;
        private final String nickname;
        public static HostInfoDTO toDTO(Member host) {
            return HostInfoDTO.builder()
                    .memberId(host.getId())
                    .nickname(host.getNickname())
                    .build();
        }
    }
}

package com.example.spot.web.dto.admin;

import com.example.spot.domain.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminResponseDTO {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class DeletedMemberListDTO {
        private final LocalDateTime deletedAt;
        private final List<DeletedMemberDTO> deletedMembers;

        public static DeletedMemberListDTO toDTO(List<Member> members) {
            return DeletedMemberListDTO.builder()
                    .deletedAt(LocalDateTime.now())
                    .deletedMembers(members.stream()
                            .map(DeletedMemberDTO::toDTO)
                            .toList())
                    .build();
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    public static class DeletedMemberDTO {

        private final Long memberId;
        private final String email;

        public static DeletedMemberDTO toDTO(Member member) {
            return DeletedMemberDTO.builder()
                    .memberId(member.getId())
                    .email(member.getEmail())
                    .build();
        }
    }
}

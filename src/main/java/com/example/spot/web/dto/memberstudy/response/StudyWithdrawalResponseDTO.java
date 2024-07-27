package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.Member;
import com.example.spot.domain.study.Study;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class StudyWithdrawalResponseDTO {

    @Getter
    @RequiredArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    public static class WithdrawalDTO {

        private final Long studyId;
        private final String studyName;
        private final Long memberId;
        private final String memberName;

        public static WithdrawalDTO toDTO(Member member, Study study) {
            return WithdrawalDTO.builder()
                    .studyId(study.getId())
                    .studyName(study.getTitle())
                    .memberId(member.getId())
                    .memberName(member.getName())
                    .build();
        }
    }
}

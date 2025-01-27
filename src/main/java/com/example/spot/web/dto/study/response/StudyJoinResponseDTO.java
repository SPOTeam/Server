package com.example.spot.web.dto.study.response;

import com.example.spot.domain.Member;
import com.example.spot.domain.study.Study;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudyJoinResponseDTO {

    @Getter
    public static class JoinDTO {

        private final Long memberId;
        private final TitleDTO study;

        @Builder(access = AccessLevel.PRIVATE)
        private JoinDTO(Long memberId, TitleDTO study) {
            this.memberId = memberId;
            this.study = study;
        }

        public static JoinDTO toDTO(Member member, Study study) {
            return JoinDTO.builder()
                    .memberId(member.getId())
                    .study(TitleDTO.toDTO(study))
                    .build();
        }
    }

    @Getter
    public static class TitleDTO {

        private final Long studyId;
        private final String title;

        @Builder(access = AccessLevel.PRIVATE)
        private TitleDTO(Long studyId, String title) {
            this.studyId = studyId;
            this.title = title;
        }

        public static TitleDTO toDTO(Study study) {
            return TitleDTO.builder()
                    .studyId(study.getId())
                    .title(study.getTitle())
                    .build();
        }
    }
}

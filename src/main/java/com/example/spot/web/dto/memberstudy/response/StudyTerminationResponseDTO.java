package com.example.spot.web.dto.memberstudy.response;

import com.example.spot.domain.enums.Status;
import com.example.spot.domain.study.Study;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class StudyTerminationResponseDTO {

    @Getter
    @RequiredArgsConstructor
    @Builder(access = AccessLevel.PRIVATE)
    public static class TerminationDTO {

        private final Long studyId;
        private final String studyName;
        private final Status status;

        public static TerminationDTO toDTO(Study study) {
            return TerminationDTO.builder()
                    .studyId(study.getId())
                    .studyName(study.getTitle())
                    .status(study.getStatus())
                    .build();
        }
    }
}

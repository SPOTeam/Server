package com.example.spot.web.dto.study.response;

import com.example.spot.domain.study.Study;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StudyRegisterResponseDTO {

    @Getter
    public static class RegisterDTO {

        private final Long studyId;
        private final String title;

        @Builder(access = AccessLevel.PRIVATE)
        private RegisterDTO(Long studyId, String title) {
            this.studyId = studyId;
            this.title = title;
        }

        public static RegisterDTO toDTO(Study study) {
            return RegisterDTO.builder()
                    .studyId(study.getId())
                    .title(study.getTitle())
                    .build();
        }
    }
}

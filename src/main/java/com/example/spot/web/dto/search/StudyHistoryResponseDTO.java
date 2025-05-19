package com.example.spot.web.dto.search;

import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Study;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;

public record StudyHistoryResponseDTO (
        Page<StudyHistoryDTO> studyHistories
) {
    public static StudyHistoryResponseDTO of(Page<MemberStudy> studies) {
        return new StudyHistoryResponseDTO(studies.map(memberStudy -> new StudyHistoryDTO(
                memberStudy.getStudy().getId(),
                memberStudy.getStudy().getTitle(),
                memberStudy.getStudy().getPerformance(),
                memberStudy.getCreatedAt(),
                memberStudy.getFinishedAt()
        )));
    }

    private record StudyHistoryDTO (
            Long studyId,
            String title,
            String performance,
            LocalDateTime createdAt,
            LocalDateTime finishedAt
    ) {
    }
}

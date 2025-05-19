package com.example.spot.web.dto.search;

import com.example.spot.domain.study.Study;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;

public record StudyHistoryResponseDTO (
        Page<StudyHistoryDTO> studyHistories
) {
    public static StudyHistoryResponseDTO of(Page<Study> studies) {
        return new StudyHistoryResponseDTO(studies.map(study -> new StudyHistoryDTO(
                study.getId(),
                study.getTitle(),
                study.getPerformance(),
                study.getCreatedAt(),
                study.getFinishedAt()
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

package com.example.spot.web.dto.study.response;

import com.example.spot.domain.enums.ApplicationStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyApplyResponseDTO {
    private ApplicationStatus status;
    private LocalDateTime updatedAt;

}

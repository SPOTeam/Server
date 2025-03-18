package com.example.spot.web.dto.post;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PostReportResponse {
    private Long reportedPostId;
    private Long reporterId;
    private LocalDateTime reportedAt;

    public static PostReportResponse toDTO(Long reportedPostId, Long reporterId) {
        return PostReportResponse.builder()
                .reportedPostId(reportedPostId)
                .reporterId(reporterId)
                .reportedAt(LocalDateTime.now())
                .build();
    }
}

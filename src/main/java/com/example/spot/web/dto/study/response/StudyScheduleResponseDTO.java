package com.example.spot.web.dto.study.response;


import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyScheduleResponseDTO {
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int size;
    private List<StudyScheduleDTO> schedules;


    public StudyScheduleResponseDTO(Page<?> page, List<StudyScheduleDTO> schedules, long totalElements){
        this.totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / page.getSize());
        this.totalElements = totalElements;
        this.first = page.isFirst();
        this.last = page.isLast();
        this.size = page.getSize();
        this.schedules = schedules;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyScheduleDTO{
        private LocalDateTime staredAt;
        private String title;
        private String province;
        private String district;
        private String neighborhood;
    }

}

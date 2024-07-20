package com.example.spot.web.dto.search;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SearchResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyPreviewDTO{
        List<SearchResponseDTO.SearchStudyDTO> studies;
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        boolean isFirst;
        boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchStudyDTO {
        Long studyId;
        String imageUrl;
        String title;
        String introduction;
        Long memberCount;
        Long heartCount;
        Long hitNum;
    }
}

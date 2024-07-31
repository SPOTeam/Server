package com.example.spot.web.dto.search;

import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.PreferredStudy;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

public class SearchResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyPageDTO{
        private String name;
        private Long appliedStudies;
        private Long ongoingStudies;
        private Long myRecruitingStudies;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyPreviewDTO {
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
        private int size;
        private List<SearchResponseDTO.SearchStudyDTO> content;
        private int number;

        public StudyPreviewDTO(Page<?> page, List<SearchResponseDTO.SearchStudyDTO> content , long totalElements) {
            this.totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / page.getSize());
            this.totalElements = totalElements;
            this.first = page.isFirst();
            this.last = page.isLast();
            this.size = page.getSize();
            this.content = content;
            this.number = page.getNumber();
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchStudyDTO {
        public SearchStudyDTO(Study study, long memberId){
            getInstructor(study);
            this.isLiked = study.getPreferredStudies().stream().map(PreferredStudy::getMember).anyMatch(s -> s.getId().equals(
                memberId));
        }
        public SearchStudyDTO(Study study){
            getInstructor(study);
        }

        private void getInstructor(Study study) {
            this.studyId = study.getId();
            this.imageUrl = study.getProfileImage();
            this.title = study.getTitle();
            this.introduction = study.getIntroduction();
            this.memberCount = (long) study.getMemberStudies().size();
            this.heartCount = (long) study.getHeartCount();
            this.hitNum = study.getHitNum();
            this.studyState = study.getStudyState();
            this.regions = study.getRegionStudies().stream().map(RegionStudy::getRegion).map(Region::getCode).toList();
            this.themeTypes = study.getStudyThemes().stream().map(StudyTheme::getTheme).map(Theme::getStudyTheme).toList();
            this.createdAt = study.getCreatedAt();
        }


        Long studyId;
        String imageUrl;
        String title;
        String introduction;
        Long memberCount;
        Long heartCount;
        Long hitNum;
        boolean isLiked;
        StudyState studyState;
        List<ThemeType> themeTypes;
        List<String> regions;
        LocalDateTime createdAt;
    }

}

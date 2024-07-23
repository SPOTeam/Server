package com.example.spot.web.dto.search;

import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import java.time.LocalDateTime;
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
    public static class SearchStudyDTO {
        public SearchStudyDTO(Study study){
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
        StudyState studyState;
        List<ThemeType> themeTypes;
        List<String> regions;
        LocalDateTime createdAt;
    }

}

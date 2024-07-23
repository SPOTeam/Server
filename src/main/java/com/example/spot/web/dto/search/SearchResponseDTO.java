package com.example.spot.web.dto.search;

import com.example.spot.domain.study.Study;
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

        }
        Long studyId;
        String imageUrl;
        String title;
        String introduction;
        Long memberCount;
        Long heartCount;
        Long hitNum;
    }

}

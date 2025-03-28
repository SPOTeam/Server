package com.example.spot.web.dto.search;

import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.StudyLikeStatus;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.PreferredStudy;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
    public static class HotKeywordDTO {
        Set<KeywordDTO> keyword;
        String updatedAt;

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class KeywordDTO{
            String keyword;
            Double point;
        }
    }

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
        private int pageNumber;

        public StudyPreviewDTO(Page<?> page, List<SearchResponseDTO.SearchStudyDTO> content , long totalElements) {
            this.totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / page.getSize());
            this.totalElements = totalElements;
            this.first = page.isFirst();
            this.last = page.isLast();
            this.size = page.getSize();
            this.content = content;
            this.pageNumber = page.getNumber();
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchStudyDTO {
        public SearchStudyDTO(Study study, long memberId){
            getInstructor(study);
            this.isLiked = study.getPreferredStudies().stream()
                .filter(preferredStudy -> preferredStudy.getStudyLikeStatus() == StudyLikeStatus.LIKE) // status가 LIKED인 경우만 필터링
                .map(PreferredStudy::getMember)
                .anyMatch(member -> member.getId().equals(memberId));
        }
        public SearchStudyDTO(Study study){
            getInstructor(study);
        }

        private void getInstructor(Study study) {
            this.studyId = study.getId();
            this.imageUrl = study.getProfileImage();
            this.title = study.getTitle();
            this.introduction = study.getIntroduction();
            this.goal = study.getGoal();
            this.memberCount = (long) study.getMemberStudies().stream()
                    .filter(memberStudy -> memberStudy.getStatus().equals(ApplicationStatus.APPROVED))
                    .toList()
                    .size();
            this.heartCount = (long) study.getHeartCount();
            this.hitNum = study.getHitNum();
            this.maxPeople = study.getMaxPeople();
            this.studyState = study.getStudyState();
            this.regions = study.getRegionStudies().stream().map(RegionStudy::getRegion).map(Region::getCode).toList();
            this.themeTypes = study.getStudyThemes().stream().map(StudyTheme::getTheme).map(Theme::getStudyTheme).toList();
            this.createdAt = study.getCreatedAt();
        }


        Long studyId;
        String imageUrl;
        String title;
        String introduction;
        String goal;
        Long memberCount;
        Long heartCount;
        Long hitNum;
        Long maxPeople;
        boolean isLiked;
        StudyState studyState;
        List<ThemeType> themeTypes;
        List<String> regions;
        LocalDateTime createdAt;
    }

}

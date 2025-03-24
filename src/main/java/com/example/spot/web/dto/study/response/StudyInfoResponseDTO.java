package com.example.spot.web.dto.study.response;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.PreferredStudy;
import com.example.spot.domain.study.Study;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
public class StudyInfoResponseDTO {

    @Getter
    public static class StudyInfoDTO {

        private final Long studyId;
        private final String studyName;
        private final StudyOwnerDTO studyOwner;
        private final Long hitNum;
        private final Integer heartCount;
        private final Integer memberCount;
        private final Long maxPeople;
        private final Gender gender;
        private final Integer minAge;
        private final Integer maxAge;
        private final Integer fee;
        private final Boolean isOnline;
        private final String profileImage;
        private final List<ThemeType> themes;
        private final List<String> regions;
        private final String goal;
        private final String introduction;

        @Builder(access = AccessLevel.PRIVATE)
        private StudyInfoDTO(Long studyId, String studyName, StudyOwnerDTO studyOwner,
                             Long hitNum, Integer heartCount, Integer memberCount, Boolean isLiked, Long maxPeople, Gender gender,
                             Integer minAge, Integer maxAge, Integer fee, Boolean isOnline, String profileImage,
                             List<ThemeType> themes, List<String> regions,String goal, String introduction) {
            this.studyId = studyId;
            this.studyName = studyName;
            this.studyOwner = studyOwner;
            this.hitNum = hitNum;
            this.heartCount = heartCount;
            this.memberCount = memberCount;
            this.maxPeople = maxPeople;
            this.gender = gender;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.fee = fee;
            this.isOnline = isOnline;
            this.profileImage = profileImage;
            this.themes = themes;
            this.regions = regions;
            this.goal = goal;
            this.introduction = introduction;
        }

        public static StudyInfoDTO toDTO(Study study, Member owner) {
            return StudyInfoDTO.builder()
                    .studyId(study.getId())
                    .studyName(study.getTitle())
                    .studyOwner(StudyOwnerDTO.toDTO(owner))
                    .hitNum(study.getHitNum())
                    .heartCount(study.getHeartCount())
                    .memberCount(
                        study.getMemberStudies().stream()
                            .filter(memberStudy -> memberStudy.getStatus().equals(ApplicationStatus.APPROVED))
                            .toList()
                            .size())
                    .maxPeople(study.getMaxPeople())
                    .gender(study.getGender())
                    .minAge(study.getMinAge())
                    .maxAge(study.getMaxAge())
                    .fee(study.getFee())
                    .isOnline(study.getIsOnline())
                    .profileImage(study.getProfileImage())
                    .themes(study.getStudyThemes().stream()
                            .map(memberStudy -> { return memberStudy.getTheme().getStudyTheme();})
                            .toList())
                    .regions(study.getRegionStudies().stream()
                            .map(memberStudy -> { return memberStudy.getRegion().getCode();})
                            .toList())
                    .goal(study.getGoal())
                    .introduction(study.getIntroduction())
                    .build();
        }
    }

    @Getter
    private static class StudyOwnerDTO {

        private final Long ownerId;
        private final String ownerName;

        @Builder
        private StudyOwnerDTO(Long ownerId, String ownerName) {
            this.ownerId = ownerId;
            this.ownerName = ownerName;
        }

        public static StudyOwnerDTO toDTO(Member member) {
            return StudyOwnerDTO.builder()
                    .ownerId(member.getId())
                    .ownerName(member.getName())
                    .build();
        }
    }

}

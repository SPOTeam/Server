package com.example.spot.web.dto.study.response;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.study.Study;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

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
        private final Gender gender;
        private final Integer minAge;
        private final Integer maxAge;
        private final Integer fee;
        private final Boolean isOnline;
        private final List<ThemeType> themes;
        private final String goal;
        private final String introduction;

        @Builder(access = AccessLevel.PRIVATE)
        private StudyInfoDTO(Long studyId, String studyName, StudyOwnerDTO studyOwner,
                             Long hitNum, Integer heartCount, Integer memberCount, Gender gender,
                             Integer minAge, Integer maxAge, Integer fee, Boolean isOnline,
                             List<ThemeType> themes, String goal, String introduction) {
            this.studyId = studyId;
            this.studyName = studyName;
            this.studyOwner = studyOwner;
            this.hitNum = hitNum;
            this.heartCount = heartCount;
            this.memberCount = memberCount;
            this.gender = gender;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.fee = fee;
            this.isOnline = isOnline;
            this.themes = themes;
            this.goal = goal;
            this.introduction = introduction;
        }

        public static StudyInfoDTO toDTO(Study study, Member member) {
            return StudyInfoDTO.builder()
                    .studyId(study.getId())
                    .studyName(study.getTitle())
                    .studyOwner(StudyOwnerDTO.toDTO(member))
                    .hitNum(study.getHitNum())
                    .heartCount(study.getHeartCount())
                    .memberCount(study.getMemberStudies().size())
                    .gender(study.getGender())
                    .minAge(study.getMinAge())
                    .maxAge(study.getMaxAge())
                    .fee(study.getFee())
                    .isOnline(study.getIsOnline())
                    .themes(study.getStudyThemes().stream()
                            .map(memberStudy -> { return memberStudy.getTheme().getStudyTheme();})
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
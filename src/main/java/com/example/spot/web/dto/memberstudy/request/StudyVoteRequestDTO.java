package com.example.spot.web.dto.memberstudy.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class StudyVoteRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteDTO {

        private String title;
        private List<String> options;
        private Boolean isMultipleChoice;
        private LocalDateTime finishedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VotedOptionDTO {
        private List<Long> optionIdList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteUpdateDTO {

        private String title;
        private List<OptionDTO> options;
        private Boolean isMultipleChoice;
        private LocalDateTime finishedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDTO {

        private Long optionId;
        private String content;
    }

}

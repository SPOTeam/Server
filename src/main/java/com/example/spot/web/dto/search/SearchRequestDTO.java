package com.example.spot.web.dto.search;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.StudySortBy;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SearchRequestDTO {

    @Getter
    @AllArgsConstructor
    public static class SearchStudyDTO{


        @Schema(description = "성별을 입력 받습니다.", example = "MALE")
        private Gender gender;

        @Schema(description = "최소 나이 (18 이상).", example = "18")
        private Integer minAge;

        @Schema(description = "최대 나이 (60 이하).", example = "60")
        private Integer maxAge;

        @Schema(description = "스터디 온라인 진행 여부 (true, false).", example = "true")
        private Boolean isOnline;

        @Schema(description = "스터디 활동비 유무 (true, false).", example = "false")
        private Boolean hasFee;

        @Schema(description = "스터디 최대 활동비.", example = "10000")
        private Integer fee;

        @Schema(description = "정렬 기준.", example = "HIT")
        private StudySortBy sortBy;
    }



}

package com.example.spot.web.dto.search;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.StudySortBy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

public class SearchRequestDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchRequestStudyDTO {


        @Schema(description = "성별을 입력 받습니다.", example = "MALE")
        private Gender gender;

        @Schema(description = "최소 나이 (18 이상).", example = "18")
        @Min(value = 18, message = "최소 나이는 18세 입니다.")
        private Integer minAge = 18;

        @Schema(description = "최대 나이 (60 이하).", example = "60")
        @Max(value = 60, message = "최대 나이는 60세 입니다.")
        private Integer maxAge = 60;

        @Schema(description = "스터디 온라인 진행 여부 (true, false).", example = "true")
        private Boolean isOnline = true;

        @Schema(description = "스터디 활동비 유무 (true, false).", example = "false")
        private Boolean hasFee = true;

        @Schema(description = "스터디 최대 활동비.", example = "10000")
        @Max(value = 1000000, message = "최대 활동비는 1,000,000원 입니다.")
        private Integer fee;
    }



}

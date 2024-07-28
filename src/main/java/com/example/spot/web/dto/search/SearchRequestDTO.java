package com.example.spot.web.dto.search;

import com.example.spot.domain.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class SearchRequestDTO {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchRequestStudyDTO {

        @Schema(description = "성별을 입력 받습니다.", example = "MALE")
        private Gender gender;

        @Schema(description = "최소 나이 (18 이상).", example = "18")
        @Min(value = 18, message = "최소 나이는 18세 입니다.")
        @NotNull(message = "최소 나이는 필수 입력 값입니다.")
        private Integer minAge;

        @Schema(description = "최대 나이 (60 이하).", example = "60")
        @Max(value = 60, message = "최대 나이는 60세 입니다.")
        @NotNull(message = "최대 나이는 필수 입력 값입니다.")
        private Integer maxAge;

        @Schema(description = "스터디 온라인 진행 여부 (true, false).", example = "true")
        private Boolean isOnline;

        @Schema(description = "스터디 활동비 유무 (true, false).", example = "false")
        private Boolean hasFee;

        @Schema(description = "스터디 최대 활동비.", example = "10000")
        @Max(value = 1000000, message = "최대 활동비는 1,000,000원 입니다.")
        private Integer fee;

        // 커스텀 검증 어노테이션
        @AssertTrue(message = "최소 나이는 최대 나이보다 작아야 합니다.")
        private boolean isValidAgeRange() {
            if (minAge == null || maxAge == null) {
                return true;
            }
            return minAge <= maxAge;
        }

        // hasFee가 false인데, fee가 있는 경우
        @AssertTrue(message = "활동비가 없는 경우에는 활동비 금액을 입력 받지 않습니다.")
        private boolean isValidFee() {
            if (hasFee == null || hasFee) {
                return true;
            }
            return fee == null;
        }
    }


}

package com.example.spot.web.dto.util.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ImageResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUploadResponse {
        private List<Images> imageUrls;
        private Integer imageCount;
    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Images {
        private String imageUrl;
        private LocalDateTime uploadAt;
    }

}

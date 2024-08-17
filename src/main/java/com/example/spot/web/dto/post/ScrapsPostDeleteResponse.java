package com.example.spot.web.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@Getter
@AllArgsConstructor
public class ScrapsPostDeleteResponse {
    private List<ScrapPostResponse> cancelScraps;
}

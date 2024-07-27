package com.example.spot.web.dto.study.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyPostResponseDTO {
    private String title;
    private String content;
}

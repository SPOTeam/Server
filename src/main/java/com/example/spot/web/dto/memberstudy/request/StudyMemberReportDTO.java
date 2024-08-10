package com.example.spot.web.dto.memberstudy.request;

import com.example.spot.validation.annotation.TextLength;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyMemberReportDTO {

    @TextLength(min = 1, max = 255)
    private String content;
}

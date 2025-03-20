package com.example.spot.web.dto.memberstudy.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyHostWithdrawRequestDTO {

    private Long newHostId;
    private String reason;

}

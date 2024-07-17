package com.example.spot.web.controller;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.ThemeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Study", description = "Study API")
@RestController
@RequestMapping("/api")
public class StudyController {

    @GetMapping("/studies")
    @Operation(summary = "전체 스터디 조회하기", description = "전체 스터디 목록을 불러옵니다.")
    public void getAllStudies(@RequestParam("gender") Gender gender,
                              @RequestParam("min-age") Integer minAge,
                              @RequestParam("max-age") Integer maxAge,
                              @RequestParam("fee") Boolean fee,
                              @RequestParam("theme") ThemeType themeType) {}

    @GetMapping("/members/{memberId}/regions/studies")
    @Operation(summary = "관심 지역별 스터디 불러오기", description = "관심 지역별 스터디 목록을 불러옵니다.")
    public void getAllRegionStudies(@PathVariable(value = "memberId", required = false) Long memberId,
                                    @RequestParam(value = "gender", required = false) Gender gender,
                                    @RequestParam(value = "min-age", required = false) Integer minAge,
                                    @RequestParam(value = "max-age", required = false) Integer maxAge,
                                    @RequestParam(value = "fee", required = false) Boolean fee,
                                    @RequestParam(value = "theme", required = false) ThemeType themeType) {}

    @GetMapping("/studies/{studyId}")
    @Operation(summary = "스터디 정보 불러오기", description = "스터디 페이지 초기화면을 불러옵니다.")
    public void getStudyInfo(@PathVariable("studyId") Long studyId) {}

}

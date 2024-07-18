package com.example.spot.web.controller;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.ThemeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Study", description = "Study API")
@RestController
@RequestMapping("/spot")
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

    /* ----------------------------- 스터디 찜하기 관련 API ------------------------------------- */

    @PostMapping("/studies/{studyId}/members/{memberId}/like")
    @Operation(summary = "스터디 찜하기 취소", description = """ 
        ## 스터디 찜하기를 누르면 해당 스터디를 찜한 회원 목록에 추가 됩니다.
        찜하기 성공 여부가 반환 됩니다.
        """)
    @Parameter(name = "studyId", description = "찜할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "memberId", description = "찜을 누를 회원의 ID를 입력 받습니다.", required = true)
    public void likeStudy(@PathVariable("studyId") Long studyId, @PathVariable("memberId") Long memberId) {
        // 메소드 구현
    }

    @DeleteMapping("/studies/{studyId}/members/{memberId}/like")
    @Operation(summary = "스터디 찜하기 취소", description = """ 
        ## 스터디 찜하기를 취소하면 해당 스터디를 찜한 회원 목록에서 삭제 됩니다.
        찜하기 취소 성공 여부가 반환 됩니다.
        """)
    @Parameter(name = "studyId", description = "찜을 취소할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "memberId", description = "찜을 취소할 회원의 ID를 입력 받습니다.", required = true)
    public void unlikeStudy(@PathVariable("studyId") Long studyId, @PathVariable("memberId") Long memberId) {
        // 메소드 구현
    }


}

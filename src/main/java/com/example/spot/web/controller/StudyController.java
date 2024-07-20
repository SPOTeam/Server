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

/* ----------------------------- 스터디 생성/참여 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 생성/참여] 스터디 정보 불러오기", description = """ 
        ## [스터디 생성/참여] 스터디 페이지 클릭, 전체 공개되는 스터디의 정보를 불러옵니다.
        스터디의 정보(이름, 참여인원, 찜 개수, 테마, 온라인 여부, 비용 여부, 연령 제한, 목표, 소개, 스터디장 등)가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}")
    public void getStudyInfo(@PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 생성/참여] 참여 신청하기", description = """ 
        ## [스터디 생성/참여] 스터디 페이지 > 신청하기 클릭, 로그인한 회원이 스터디에 신청합니다.
        로그인한 회원이 member_study에 application_status = APPLIED 상태로 추가됩니다.
        """)
    @PostMapping("/members/{memberId}/studies/{studyId}")
    public void applyToStudy(@PathVariable Long memberId, @PathVariable Long studyId) {
    }

    @Operation(summary = "[스터디 생성/참여] 스터디 등록하기", description = """ 
        ## [스터디 생성/참여] 스터디 등록 페이지 클릭, 로그인한 회원이 스터디를 등록합니다.
        로그인한 회원이 owner인 새로운 스터디가 study에 생성됩니다.
        """)
    @PostMapping("/members/{memberId}/studies")
    public void registerStudy(@PathVariable Long memberId) {
    }

/* ----------------------------- 스터디 찜하기 관련 API ------------------------------------- */

    @PostMapping("/studies/{studyId}/members/{memberId}/like")
    @Operation(summary = "[스터디 찜하기] 스터디 찜하기 ", description = """ 
        ## [스터디 찜하기] 스터디 찜하기를 누르면 해당 스터디를 찜한 회원 목록에 추가 됩니다.
        찜하기 성공 여부가 반환 됩니다.
        """)
    @Parameter(name = "studyId", description = "찜할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "memberId", description = "찜을 누를 회원의 ID를 입력 받습니다.", required = true)
    public void likeStudy(@PathVariable("studyId") Long studyId, @PathVariable("memberId") Long memberId) {
        // 메소드 구현
    }

    @DeleteMapping("/studies/{studyId}/members/{memberId}/like")
    @Operation(summary = "[스터디 찜하기] 스터디 찜하기 취소", description = """ 
        ## [스터디 찜하기] 스터디 찜하기를 취소하면 해당 스터디를 찜한 회원 목록에서 삭제 됩니다.
        찜하기 취소 성공 여부가 반환 됩니다.
        """)
    @Parameter(name = "studyId", description = "찜을 취소할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "memberId", description = "찜을 취소할 회원의 ID를 입력 받습니다.", required = true)
    public void unlikeStudy(@PathVariable("studyId") Long studyId, @PathVariable("memberId") Long memberId) {
        // 메소드 구현
    }


}

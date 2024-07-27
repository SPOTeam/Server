package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.study.StudyCommandService;
import com.example.spot.service.study.StudyQueryService;
import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.validation.annotation.ExistStudy;
import com.example.spot.web.dto.study.request.StudyJoinRequestDTO;
import com.example.spot.web.dto.study.request.StudyRegisterRequestDTO;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;
import com.example.spot.web.dto.study.response.StudyJoinResponseDTO;
import com.example.spot.web.dto.study.response.StudyLikeResponseDTO;
import com.example.spot.web.dto.study.response.StudyRegisterResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Study", description = "Study API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
@Validated
public class StudyController {

    private final StudyQueryService studyQueryService;
    private final StudyCommandService studyCommandService;

/* ----------------------------- 스터디 생성/참여 관련 API ------------------------------------- */

    @Operation(summary = "[스터디 생성/참여] 스터디 정보 불러오기", description = """ 
        ## [스터디 생성/참여] 스터디 페이지 클릭, 전체 공개되는 스터디의 정보를 불러옵니다.
        스터디의 정보(이름, 참여인원, 찜 개수, 테마, 온라인 여부, 비용 여부, 연령 제한, 목표, 소개, 스터디장 등)가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}")
    public ApiResponse<StudyInfoResponseDTO.StudyInfoDTO> getStudyInfo(@PathVariable Long studyId) {
//        StudyInfoResponseDTO.StudyInfoDTO studyInfoDTO = studyQueryService.getStudyInfo(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_FOUND, null);
    }

    @Operation(summary = "[스터디 생성/참여] 참여 신청하기", description = """ 
        ## [스터디 생성/참여] 스터디 페이지 > 신청하기 클릭, 로그인한 회원이 스터디에 신청합니다.
        로그인한 회원이 member_study에 application_status = APPLIED 상태로 추가됩니다.
        """)
    @PostMapping("/members/{memberId}/studies/{studyId}")
    public ApiResponse<StudyJoinResponseDTO.JoinDTO> applyToStudy(@PathVariable Long memberId, @PathVariable Long studyId,
                                                                  @RequestBody StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO) {
        StudyJoinResponseDTO.JoinDTO studyJoinResponseDTO = studyCommandService.applyToStudy(memberId, studyId, studyJoinRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_CREATED, studyJoinResponseDTO);
    }

    @Operation(summary = "[스터디 생성/참여] 스터디 등록하기", description = """ 
        ## [스터디 생성/참여] 스터디 등록 페이지 클릭, 로그인한 회원이 스터디를 등록합니다.
        로그인한 회원이 owner인 새로운 스터디가 study에 생성됩니다.
        """)
    @PostMapping("/members/{memberId}/studies")
    public ApiResponse<StudyRegisterResponseDTO.RegisterDTO> registerStudy(@PathVariable Long memberId, @RequestBody StudyRegisterRequestDTO.RegisterDTO studyRegisterRequestDTO) {
        StudyRegisterResponseDTO.RegisterDTO studyRegisterResponseDTO = studyCommandService.registerStudy(memberId, studyRegisterRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_CREATED, studyRegisterResponseDTO);
    }

/* ----------------------------- 스터디 찜하기 관련 API ------------------------------------- */

    @PostMapping("/studies/{studyId}/members/{memberId}/like")
    @Operation(summary = "[스터디 찜하기] 스터디 찜하기 ", description = """ 
        ## [스터디 찜하기] 해당 스터디를 찜하지 않은 상태에서 버튼을 누르면 해당 스터디를 찜 하게 됩니다.
        반대로, 찜한 상태에서 버튼을 누르면 찜을 취소하게 됩니다.
       
        찜한 스터디 제목과 찜 생성 시간, 찜 상태가 반환 됩니다.
        """)
    @Parameter(name = "studyId", description = "찜할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "memberId", description = "찜을 누를 회원의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyLikeResponseDTO> likeStudy(
        @PathVariable("studyId") @ExistStudy Long studyId,
        @PathVariable("memberId") @ExistMember Long memberId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_LIKED, studyCommandService.likeStudy(memberId, studyId));
    }
}

package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.enums.Theme;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.memberstudy.MemberStudyCommandService;
import com.example.spot.service.memberstudy.MemberStudyQueryService;
import com.example.spot.validation.annotation.*;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.memberstudy.request.*;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListRequestDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListRequestDTO.ToDoListCreateDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListCreateResponseDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListSearchResponseDTO;
import com.example.spot.web.dto.memberstudy.request.toDo.ToDoListResponseDTO.ToDoListUpdateResponseDTO;
import com.example.spot.web.dto.memberstudy.response.*;
import com.example.spot.web.dto.study.request.StudyRegisterRequestDTO;
import com.example.spot.web.dto.study.response.*;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO;
import com.example.spot.web.dto.study.response.StudyMemberResponseDTO.StudyApplicantDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
@Validated
public class MemberStudyController {

    private final MemberStudyQueryService memberStudyQueryService;
    private final MemberStudyCommandService memberStudyCommandService;

/* ----------------------------- 진행중인 스터디 관련 API ------------------------------------- */


    @Tag(name = "진행중인 스터디")
    @Operation(summary = "[진행중인 스터디] 스터디 탈퇴하기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 현재 진행중인 스터디에서 탈퇴합니다.
        로그인한 회원이 참여하는 특정 스터디에 대해 member_study 튜플을 삭제합니다.
        """)
    @DeleteMapping("/studies/{studyId}/withdrawal")
    public ApiResponse<StudyWithdrawalResponseDTO.WithdrawalDTO> withdrawFromStudy(@PathVariable Long studyId) {
        StudyWithdrawalResponseDTO.WithdrawalDTO withdrawalDTO = memberStudyCommandService.withdrawFromStudy(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_DELETED, withdrawalDTO);
    }

    @Tag(name = "진행중인 스터디")
    @Operation(summary = "[진행중인 스터디] 스터디 호스트 탈퇴",
            description = """
               ## [진행중인 스터디] 특정 스터디의 호스트가 해당 스터디에서 탈퇴합니다.
               탈퇴 시, 호스트 권한이 회수되며 스터디에서 제외됩니다. 
               요청 시, 새로운 호스트의 아이디와 임명 사유를 입력해야 합니다.
           """)
    @DeleteMapping("/studies/{studyId}/hosts/withdrawal")
    public ApiResponse<StudyWithdrawalResponseDTO.WithdrawalDTO> withdrawHostFromStudy(
            @PathVariable Long studyId,
            @RequestBody StudyHostWithdrawRequestDTO requestDTO) {
        StudyWithdrawalResponseDTO.WithdrawalDTO withdrawalDTO =
                memberStudyCommandService.withdrawHostFromStudy(studyId, requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_DELETED, withdrawalDTO);
    }


    @Tag(name = "진행중인 스터디")
    @Operation(summary = "[진행중인 스터디] 스터디 끝내기", description = """ 
        ## [진행중인 스터디] 마이페이지 > 진행중 > 진행중인 스터디의 메뉴 클릭, 로그인한 회원이 운영중인 스터디를 끝냅니다.
        * 로그인한 회원이 운영하는 특정 스터디에 대해 study status OFF로 전환합니다.
        * 스터디 성과를 입력받아 DB에 저장합니다.
        """)
    @PatchMapping("/studies/{studyId}/termination")
    public ApiResponse<StudyTerminationResponseDTO.TerminationDTO> terminateStudy(
            @PathVariable @ExistStudy Long studyId,
            @RequestParam @TextLength(min=1, max=30) String performance
    ) {
        StudyTerminationResponseDTO.TerminationDTO terminationDTO = memberStudyCommandService.terminateStudy(studyId, performance);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_TERMINATED, terminationDTO);
    }


/* ----------------------------- 모집중인 스터디 관련 API ------------------------------------- */

    @Tag(name = "모집중인 스터디")
    @Operation(summary = "[모집중인 스터디] 스터디 별 신청 여부 조회하기", description = """ 
        ## [모집중인 스터디] 로그인한 회원이 모집중인 스터디에 대해 신청 여부를 조회합니다.
        로그인한 회원이 참여하는 특정 스터디에 대해 member_study의 application_status가 APPLIED인지 확인합니다.
        반환 값은 boolean으로, 신청 여부를 나타냅니다.
        true: 신청한 상태, false: 신청하지 않은 상태
        """)
    @GetMapping("/studies/{studyId}/is-applied")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyApplicantDTO> getIsApplied(@PathVariable @ExistStudy Long studyId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_FOUND,
            memberStudyQueryService.isApplied(studyId));
    }

    @Tag(name = "모집중인 스터디")
    @Operation(summary = "[모집중인 스터디] 스터디별 신청 회원 목록 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원 목록을 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 대해 member_study의 application_status가 APPLIED인 회원 목록이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyMemberResponseDTO> getAllApplicants(@PathVariable @ExistStudy Long studyId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_FOUND,
            memberStudyQueryService.findStudyApplicants(studyId));
    }
    @Tag(name = "모집중인 스터디")
    @Operation(summary = "[모집중인 스터디] 스터디 신청 정보(이름, 자기소개) 불러오기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원의 정보를 불러옵니다.
        로그인한 회원이 모집중인 특정 스터디에 신청한 회원의 정보(member.name & member_study.introduction)가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/applicants/{applicantId}")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "applicantId", description = "신청자의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyMemberResponseDTO.StudyApplyMemberDTO> getApplicantInfo(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long applicantId) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_FOUND,
            memberStudyQueryService.findStudyApplication(studyId, applicantId));
    }
    @Tag(name = "모집중인 스터디")
    @Operation(summary = "[모집중인 스터디] 스터디 신청 처리하기", description = """ 
        ## [모집중인 스터디] 마이페이지 > 모집중 > 스터디 > 신청 회원 > 거절 클릭, 로그인한 회원이 모집중인 스터디에 신청한 회원을 처리합니다.
        isAccept가 true인 경우 member_study에서 application_status를 AWAITING_SELF_APPROVAL 수정합니다. -> 참가 희망하는 회원이 알림을 통해 스스로 승인 해야 스터디 참여가 완료됩니다.
        isAccept가 false인 경우 member_study에서 application_status를 REJECTED로 수정합니다.
        스터디 신청 처리 결과를 응답으로 반환합니다. 
        """)
    @PostMapping("/studies/{studyId}/applicants/{applicantId}")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "applicantId", description = "신청자의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyApplyResponseDTO> rejectApplicant(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long applicantId,
        @RequestParam boolean isAccept) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_UPDATED,
            memberStudyCommandService.acceptAndRejectStudyApply(applicantId, studyId, isAccept));
    }

    @Tag(name = "테스트 용 API", description = "테스트 용 API")
    @Operation(summary = "!테스트 용! [모집중인 스터디] 스터디 신청 처리하기", description = """ 
        ## [모집중인 스터디] 빠른 API 적용를 위해 스터디 신청 처리를 즉시 수행합니다.
        위 API와 동일한 기능을 수행하지만, 실제 알림이 발송되지 않습니다. 
        또한 스터디 신청 승인 시,  회원의 상태가 AWAITING_SELF_APPROVAL가 아닌 APPROVED로 변경됩니다.
       
        즉, 바로 스터디 참여가 완료됩니다. 스터디 회원 조회 시, 바로 승인된 회원으로 조회됩니다.
        
        isAccept가 true인 경우 member_study에서 application_status를 APPROVED로 수정합니다.
        isAccept가 false인 경우 member_study에서 application_status를 REJECTED로 수정합니다.
        스터디 신청 처리 결과를 응답으로 반환합니다.
        """)
    @PostMapping("/studies/{studyId}/applicants/{applicantId}/test")
    @Parameter(name = "studyId", description = "모집중인 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "applicantId", description = "신청자의 ID를 입력 받습니다.", required = true)
    public ApiResponse<StudyApplyResponseDTO> rejectApplicantForTest(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long applicantId,
        @RequestParam boolean isAccept) {
        return ApiResponse.onSuccess(SuccessStatus._STUDY_APPLICANT_UPDATED,
            memberStudyCommandService.acceptAndRejectStudyApplyForTest(applicantId, studyId, isAccept));
    }


/* ----------------------------- 스터디 상세 정보 관련 API ------------------------------------- */

    @Tag(name = "스터디 상세 정보")
    @Operation(summary = "[스터디 상세 정보] 스터디 최근 공지 1개 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 최근 공지 1개를 불러옵니다.
        study_post의 announced_at이 가장 최근인 공지 1개가 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/announce")
    public ApiResponse<StudyPostResponseDTO> getRecentAnnouncement(@PathVariable @ExistStudy Long studyId) {
        StudyPostResponseDTO studyPostResponseDTO = memberStudyQueryService.findStudyAnnouncementPost(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_FOUND, studyPostResponseDTO);
    }
    @Tag(name = "스터디 상세 정보")
    @Operation(summary = "[스터디 상세 정보] 다가오는 모임 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 내 스터디 > 스터디 클릭, 로그인한 회원이 참여하는 특정 스터디의 다가오는 모임 목록을 페이징 조회 합니다.
        현재 시점 이후에 진행되는 모임 일정의 목록을 schedule에서 반환합니다.
        """)
    @GetMapping("/studies/{studyId}/upcoming-schedules")
    public ApiResponse<StudyScheduleResponseDTO> getUpcomingSchedules(
        @PathVariable @ExistStudy Long studyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "1") int size){
        StudyScheduleResponseDTO studyScheduleResponseDTO = memberStudyQueryService.findStudySchedule(studyId, PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_FOUND, studyScheduleResponseDTO);
    }
    @Tag(name = "스터디 상세 정보")
    @Operation(summary = "[스터디 상세 정보] 스터디에 참여하는 회원 목록 불러오기", description = """ 
        ## [스터디 상세 정보] 로그인한 회원이 참여하는 특정 스터디의 회원 목록을 전체 조회 합니다.
        member_study에서 application_status=APPROVED인 회원의 목록(이름, 프로필 사진 포함)이 반환됩니다.
        """)
    @GetMapping("/studies/{studyId}/members")
    public ApiResponse<StudyMemberResponseDTO> getStudyMembers(
        @PathVariable @ExistStudy Long studyId){
        StudyMemberResponseDTO studyMemberResponseDTO = memberStudyQueryService.findStudyMembers(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_FOUND, studyMemberResponseDTO);
    }

    @Tag(name = "스터디 상세 정보")
    @Operation(summary = "[스터디 상세 정보] 스터디 호스트 정보 불러오기", description = """ 
        ## [스터디 상세 정보] 로그인한 회원이 참여하는 특정 스터디의 호스트 정보를 조회합니다.
        * isOwned : 로그인한 회원이 호스트인지 true or false로 반환
        * host : 호스트의 id와 nickname 반환
        """)
    @GetMapping("/studies/{studyId}/host")
    public ApiResponse<StudyMemberResDTO.StudyHostDTO> getStudyHost(
            @PathVariable @ExistStudy Long studyId)
    {
        StudyMemberResDTO.StudyHostDTO studyHostDTO = memberStudyQueryService.getStudyHost(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_HOST_FOUND, studyHostDTO);
    }


/* ----------------------------- 스터디 일정 관련 API ------------------------------------- */

    @Tag(name = "스터디 일정")
    @Operation(summary = "[스터디 일정] 월별 일정 불러오기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 클릭, 로그인한 회원이 참여하는 특정 스터디의 일정을 월 단위로 불러옵니다.
        처음 캘린더를 클릭하면 오늘 날짜가 포함된 연/월에 해당하는 일정 목록이 schedule에서 반환됩니다.
        캘린더를 넘기면 해당 연/월에 해당하는 일정 목록이 schedule에서 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "일정을 불러올 스터디의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/schedules")
    public ApiResponse<ScheduleResponseDTO.MonthlyScheduleListDTO> getMonthlySchedules(
            @PathVariable @ExistStudy Long studyId,
            @RequestParam @IntSize(min = 1) Integer year,
            @RequestParam @IntSize(min = 1, max= 12) Integer month) {
        ScheduleResponseDTO.MonthlyScheduleListDTO monthlyScheduleDTO = memberStudyQueryService.getMonthlySchedules(studyId, year, month);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_FOUND, monthlyScheduleDTO);
    }

    @Tag(name = "스터디 일정")
    @Operation(summary = "[스터디 일정] 상세 일정 불러오기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 > 일정 클릭, 로그인한 회원이 참여하는 특정 스터디의 상세 일정을 불러옵니다.
        스터디의 일정 정보를 상세하게 불러옵니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "scheduleId", description = "불러올 스터디 일정의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/schedules/{scheduleId}")
    public ApiResponse<ScheduleResponseDTO.MonthlyScheduleDTO> getSchedule(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistSchedule Long scheduleId) {
        ScheduleResponseDTO.MonthlyScheduleDTO scheduleDTO = memberStudyQueryService.getSchedule(studyId, scheduleId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_FOUND, scheduleDTO);
    }

    @Tag(name = "스터디 일정")
    @Operation(summary = "[스터디 일정] 일정 추가하기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 > 추가 버튼 클릭, 로그인한 회원이 운영하는 특정 스터디에 일정을 추가합니다.
        로그인한 회원이 owner인 경우 schedule에 새로운 일정을 등록합니다.
        
        period에는 [NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY] 중 하나를 입력해야 합니다.
        """)
    @Parameter(name = "studyId", description = "일정을 추가할 스터디의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/schedules")
    public ApiResponse<ScheduleResponseDTO.ScheduleDTO> addSchedule(
            @PathVariable @ExistStudy Long studyId,
            @RequestBody @Valid ScheduleRequestDTO.ScheduleDTO scheduleRequestDTO) {
        ScheduleResponseDTO.ScheduleDTO scheduleResponseDTO = memberStudyCommandService.addSchedule(studyId, scheduleRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_CREATED, scheduleResponseDTO);
    }

    @Tag(name = "스터디 일정")
    @Operation(summary = "[스터디 일정] 일정 변경하기", description = """ 
        ## [스터디 일정] 내 스터디 > 스터디 > 캘린더 > 일정 클릭, 로그인한 회원이 특정 스터디에 등록한 일정을 수정합니다.
        로그인한 회원이 owner인 경우 schedule에 등록한 일정을 수정할 수 있습니다.
        
        period에는 [NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY] 중 하나를 입력해야 합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "scheduleId", description = "변경할 일정의 id를 입력합니다.", required = true)
    @PatchMapping("/studies/{studyId}/schedules/{scheduleId}")
    public ApiResponse<ScheduleResponseDTO.ScheduleDTO> modSchedule(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistSchedule Long scheduleId,
            @RequestBody @Valid ScheduleRequestDTO.ScheduleDTO scheduleModDTO) {
        ScheduleResponseDTO.ScheduleDTO scheduleResponseDTO = memberStudyCommandService.modSchedule(studyId, scheduleId, scheduleModDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_SCHEDULE_UPDATED, scheduleResponseDTO);
    }


/* ----------------------------- 스터디 투표 관련 API ------------------------------------- */

    @Tag(name = "스터디 투표")
    @Operation(summary = "[스터디 투표] 투표 생성하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 작성 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 새로운 투표를 등록합니다.
        스터디에 참여하는 회원이 생성한 투표를 vote에 저장합니다.
        """)
    @Parameter(name = "studyId", description = "투표를 생성할 스터디의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/votes")
    public ApiResponse<StudyVoteResponseDTO.VotePreviewDTO> createVote(
            @PathVariable @ExistStudy Long studyId,
            @RequestBody @Valid StudyVoteRequestDTO.VoteDTO voteDTO) {
        StudyVoteResponseDTO.VotePreviewDTO votePreviewDTO = memberStudyCommandService.createVote(studyId, voteDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_CREATED, votePreviewDTO);
    }

    @Tag(name = "스터디 투표")
    @Operation(summary = "[스터디 투표] 투표하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 특정 투표 클릭, 로그인한 회원이 참여하는 스터디에서 특정 항목에 투표합니다.
        member_vote에 투표 정보를 저장합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "voteId", description = "참여할 스터디 투표의 id를 입력합니다.")
    @PostMapping("/studies/{studyId}/votes/{voteId}/options")
    public ApiResponse<StudyVoteResponseDTO.VotedOptionDTO> vote(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistVote Long voteId,
            @RequestBody @Valid StudyVoteRequestDTO.VotedOptionDTO votedOptionDTO) {
        StudyVoteResponseDTO.VotedOptionDTO votedOptionResDTO = memberStudyCommandService.vote(studyId, voteId, votedOptionDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_PARTICIPATED, votedOptionResDTO);
    }

    @Tag(name = "스터디 투표")
    @Operation(summary = "[스터디 투표] 투표 편집하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 편집하기 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 투표 정보를 수정합니다.
        스터디에 참여하는 회원이 생성한 투표를 vote에 저장합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "voteId", description = "편집할 스터디 투표의 id를 입력합니다.")
    @PatchMapping("/studies/{studyId}/votes/{voteId}")
    public ApiResponse<StudyVoteResponseDTO.VotePreviewDTO> updateVote(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistVote Long voteId,
            @RequestBody @Valid StudyVoteRequestDTO.VoteUpdateDTO voteDTO) {
        StudyVoteResponseDTO.VotePreviewDTO votePreviewDTO = memberStudyCommandService.updateVote(studyId, voteId, voteDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_UPDATED, votePreviewDTO);
    }

    @Tag(name = "스터디 투표")
    @Operation(summary = "[스터디 투표] 투표 삭제하기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 삭제하기 버튼 클릭, 로그인한 회원이 참여하는 특정 스터디에서 투표를 삭제합니다.
        스터디에 참여하는 회원이 생성한 투표를 vote에 저장합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "voteId", description = "삭제할 스터디 투표의 id를 입력합니다.")
    @DeleteMapping("/studies/{studyId}/votes/{voteId}")
    public ApiResponse<StudyVoteResponseDTO.VotePreviewDTO> deleteVote(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistVote Long voteId) {
        StudyVoteResponseDTO.VotePreviewDTO votePreviewDTO = memberStudyCommandService.deleteVote(studyId, voteId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_DELETED, votePreviewDTO);
    }

    @Tag(name = "스터디 투표")
    @Operation(summary = "[스터디 투표] 투표 목록 불러오기", description = """
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표 목록을 불러옵니다.
        진행 중(finished_at 이전)인 투표 목록과 마감(finished_at 이후)된 투표 목록을 구분하여 반환합니다.
        """)
    @Parameter(name = "studyId", description = "투표 목록을 불러올 스터디의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/votes")
    public ApiResponse<StudyVoteResponseDTO.VoteListDTO> getAllVotes(
            @PathVariable @ExistStudy Long studyId) {
        StudyVoteResponseDTO.VoteListDTO voteListDTO = memberStudyQueryService.getAllVotes(studyId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_FOUND, voteListDTO);
    }

    @Tag(name = "스터디 투표")
    @Operation(summary = "[스터디 투표] 투표 불러오기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 특정 투표 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표를 불러옵니다.
        진행중인 투표 : 진행중인 투표에 대한 항목 및 기본 정보가 반환됩니다.
        마감된 투표 : 마감된 투표에 대한 항목과 투표 인원수가 반환됩니다.
        (진행중인 투표인지 마감된 투표인지에 따라 Response DTO가 서로 다릅니다.)
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "voteId", description = "불러올 스터디 투표의 id를 입력합니다.")
    @GetMapping("/studies/{studyId}/votes/{voteId}")
    public ApiResponse<?> getVote(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistVote Long voteId) {
        Boolean isCompleted = memberStudyQueryService.getIsCompleted(voteId);
        if (isCompleted) {
            StudyVoteResponseDTO.CompletedVoteDTO completedVoteDTO = memberStudyQueryService.getVoteInCompletion(studyId, voteId);
            return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_FOUND, completedVoteDTO);
        } else {
            StudyVoteResponseDTO.VoteDTO voteDTO = memberStudyQueryService.getVoteInProgress(studyId, voteId);
            return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_FOUND, voteDTO);
        }
    }

    @Tag(name = "스터디 투표")
    @Operation(summary = "[스터디 투표] 마감된 투표 현황 불러오기", description = """ 
        ## [스터디 투표] 내 스터디 > 스터디 > 투표 > 마감된 투표 > n명 참여 클릭, 로그인한 회원이 참여하는 특정 스터디의 투표를 불러옵니다.
        마감된 투표에 대하여 항목별 투표 회원 목록을 반환합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "voteId", description = "마감된 스터디 투표의 id를 입력합니다.")
    @GetMapping("/studies/{studyId}/votes/{voteId}/details")
    public ApiResponse<StudyVoteResponseDTO.CompletedVoteDetailDTO> getCompletedVoteDetail(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistVote Long voteId) {
        StudyVoteResponseDTO.CompletedVoteDetailDTO completedVoteDetailDTO = memberStudyQueryService.getCompletedVoteDetail(studyId, voteId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_VOTE_DETAIL_STATUS_FOUND, completedVoteDetailDTO);
    }

/* ----------------------------- 스터디 갤러리 관련 API ------------------------------------- */
    @Tag(name = "스터디 이미지")
    @Operation(summary = "[스터디 갤러리] 스터디 이미지 목록 불러오기", description = """ 
        ## [스터디 갤러리] 내 스터디 > 스터디 > 갤러리 클릭, 로그인한 회원이 참여하는 스터디의 이미지 목록을 불러옵니다.
        study_post에 존재하는 모든 게시글의 이미지를 최신순으로 반환합니다.
        """)
    @Parameter(name = "studyId", description = "이미지 목록을 불러올 스터디의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/images")
    public ApiResponse<StudyImageResponseDTO.ImageListDTO> getAllStudyImages(
            @PathVariable @ExistStudy Long studyId,
            @RequestParam @Min(0) Integer offset,
            @RequestParam @Min(1) Integer limit) {
        StudyImageResponseDTO.ImageListDTO imageListDTO = memberStudyQueryService.getAllStudyImages(studyId, PageRequest.of(offset, limit));
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_IMAGES_FOUND, imageListDTO);
    }

/* ----------------------------- 스터디 출석체크 관련 API ------------------------------------- */

    @Tag(name = "스터디 출석체크")
    @Operation(summary = "[스터디 출석체크] 출석 퀴즈 생성하기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 출석체크 > 퀴즈 만들기 클릭, 로그인한 회원이 운영하는 스터디에 퀴즈를 생성합니다.
        * 로그인한 회원이 스터디장인 경우 quiz에 새로운 퀴즈를 생성합니다.
        * createdAt에는 출석 퀴즈를 생성할 날짜를 입력합니다.
        """)
    @Parameter(name = "studyId", description = "출석 퀴즈를 생성할 스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "scheduleId", description = "출석 퀴즈를 생성할 일정의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/schedules/{scheduleId}/quiz")
    public ApiResponse<StudyQuizResponseDTO.QuizDTO> createAttendanceQuiz(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistSchedule Long scheduleId,
            @RequestBody @Valid StudyQuizRequestDTO.QuizDTO quizRequestDTO) {
        StudyQuizResponseDTO.QuizDTO quizResponseDTO = memberStudyCommandService.createAttendanceQuiz(studyId, scheduleId, quizRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_QUIZ_CREATED, quizResponseDTO);
    }

    @Tag(name = "스터디 출석체크")
    @Operation(summary = "[스터디 출석체크] 출석 퀴즈 불러오기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 출석체크, 로그인한 회원이 참여하는 스터디의 퀴즈를 불러옵니다.
        * 날짜에 해당하는 퀴즈의 아이디와 질문이 반환됩니다.
        * date에는 출석 퀴즈를 불러올 날짜를 입력합니다.
        """)
    @Parameter(name = "studyId", description = "출석 퀴즈를 불러올 스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "scheduleId", description = "출석 퀴즈를 불러올 일정의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/schedules/{scheduleId}/quiz")
    public ApiResponse<StudyQuizResponseDTO.QuizDTO> getAttendanceQuiz(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistSchedule Long scheduleId,
            @RequestParam LocalDate date) {
        StudyQuizResponseDTO.QuizDTO quizDTO = memberStudyQueryService.getAttendanceQuiz(studyId, scheduleId, date);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_QUIZ_FOUND, quizDTO);
    }


    @Tag(name = "스터디 출석체크")
    @Operation(summary = "[스터디 출석체크] 출석 체크하기", description = """ 
        ## [스터디 출석체크] 내 스터디 > 스터디 > 캘린더 > 이미지 클릭, 로그인한 회원이 참여하는 스터디에서 오늘의 퀴즈를 풀어 출석을 체크합니다.
        * 특정 시점의 quiz에 대해 member_attendance 튜플을 추가합니다.
        * dateTime에는 출석을 체크할 날짜와 시간을 입력합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "scheduleId", description = "일정의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/schedules/{scheduleId}/attendance")
    public ApiResponse<StudyQuizResponseDTO.AttendanceDTO> attendantStudy(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistSchedule Long scheduleId,
            @RequestBody @Valid StudyQuizRequestDTO.AttendanceDTO attendanceRequestDTO) {
        StudyQuizResponseDTO.AttendanceDTO attendanceResponseDTO = memberStudyCommandService.attendantStudy(studyId, scheduleId, attendanceRequestDTO);
        if (attendanceResponseDTO.getIsCorrect()) {
            return ApiResponse.onSuccess(SuccessStatus._STUDY_ATTENDANCE_CREATED_CORRECT_ANSWER, attendanceResponseDTO);
        } else {
            return ApiResponse.onSuccess(SuccessStatus._STUDY_ATTENDANCE_CREATED_WRONG_ANSWER, attendanceResponseDTO);
        }
    }

    @Tag(name = "스터디 출석체크")
    @Operation(summary = "[스터디 출석체크] 출석 퀴즈 삭제하기", description = """ 
        ## [스터디 출석체크] 기한이 지난 출석 퀴즈를 삭제합니다. (화면 X)
        * PathVariable을 통해 전달받은 정보를 바탕으로 출석 퀴즈를 삭제합니다.
        * 출석 퀴즈 정보와 함께 퀴즈에 대한 MemberAttendance(회원 출석) 목록도 함께 삭제됩니다.
        * date에는 출석 퀴즈를 삭제할 날짜를 입력합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "scheduleId", description = "일정의 id를 입력합니다.", required = true)
    @DeleteMapping("/studies/{studyId}/schedules/{scheduleId}/quiz")
    public ApiResponse<StudyQuizResponseDTO.QuizDTO> deleteAttendanceQuiz(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistSchedule Long scheduleId,
            @RequestParam LocalDate date) {
        StudyQuizResponseDTO.QuizDTO quizDTO = memberStudyCommandService.deleteAttendanceQuiz(studyId, scheduleId, date);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_QUIZ_DELETED, quizDTO);
    }

    @Tag(name = "스터디 출석체크")
    @Operation(summary = "[스터디 출석체크] 회원 출석부 불러오기", description = """ 
        ## [스터디 출석체크] 지정된 날짜의 모든 스터디 회원의 출석 여부를 불러옵니다.
        * 출석체크 화면에 표시되는 스터디 회원 정보(프로필 사진, 이름, 출석 여부, 스터디장 여부) 목록를 반환합니다.
        * date에는 출석 정보를 확인할 날짜를 입력합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "scheduleId", description = "출석을 확인할 일정의 id를 입력합니다.", required = true)
    @GetMapping("/studies/{studyId}/schedules/{scheduleId}/attendance")
    public ApiResponse<StudyQuizResponseDTO.AttendanceListDTO> getAllAttendances(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistSchedule Long scheduleId,
            @RequestParam LocalDate date) {
        StudyQuizResponseDTO.AttendanceListDTO attendanceListDTO = memberStudyQueryService.getAllAttendances(studyId, scheduleId, date);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_ATTENDANCES_FOUND, attendanceListDTO);
    }



/* ----------------------------- 스터디 회원 신고 관련 API ------------------------------------- */

    @Tag(name = "스터디 신고")
    @Operation(summary = "[스터디 신고] 스터디원 신고하기", description = """ 
        ## [스터디 신고] 로그인한 회원이 참여하는 스터디의 다른 회원을 신고합니다.
        신고당한 회원의 id와 이름이 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "memberId", description = "신고할 스터디원의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/members/{memberId}/reports")
    public ApiResponse<MemberResponseDTO.ReportedMemberDTO> reportStudyMember(
            @PathVariable @ExistStudy Long studyId, @PathVariable @ExistMember Long memberId,
            @RequestBody @Valid StudyMemberReportDTO studyMemberReportDTO) {
        MemberResponseDTO.ReportedMemberDTO reportedMemberDTO = memberStudyCommandService.reportStudyMember(studyId, memberId, studyMemberReportDTO);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_MEMBER_REPORTED, reportedMemberDTO);
    }

    @Tag(name = "스터디 신고")
    @Operation(summary = "[스터디 신고] 스터디 게시글 신고하기", description = """ 
        ## [스터디 신고] 로그인한 회원이 참여하는 스터디의 게시글을 신고합니다.
        신고당한 스터디 게시글의 id와 제목이 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "postId", description = "신고할 스터디 게시글의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/posts/{postId}/reports")
    public ApiResponse<StudyPostResDTO.PostPreviewDTO> reportStudyPost(
            @PathVariable @ExistStudy Long studyId,
            @PathVariable @ExistStudyPost Long postId) {
        StudyPostResDTO.PostPreviewDTO postPreviewDTO = memberStudyCommandService.reportStudyPost(studyId, postId);
        return ApiResponse.onSuccess(SuccessStatus._STUDY_POST_REPORTED, postPreviewDTO);
    }

    /* ----------------------------- To-do list 관련 API ------------------------------------- */

    @Tag(name = "To-Do List")
    @Operation(summary = "[To-Do List] To-Do List 생성", description = """ 
        ## [To-Do List] 로그인한 회원이 참여하는 스터디에 To-Do List를 생성합니다.
        To-Do List의 id와 제목, 생성 시간이 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/to-do")
    public ApiResponse<ToDoListCreateResponseDTO> createToDoList(
        @PathVariable @ExistStudy Long studyId,
        @RequestBody @Valid ToDoListRequestDTO.ToDoListCreateDTO request) {
        ToDoListCreateResponseDTO toDoList = memberStudyCommandService.createToDoList(studyId,
            request);
        return ApiResponse.onSuccess(SuccessStatus._TO_DO_LIST_CREATED, toDoList);
    }

    @Tag(name = "To-Do List")
    @Operation(summary = "[To-Do List] To-Do List 내용 수정", description = """ 
        ## [To-Do List] To-Do List에 작성한 할 일의 내용을 수정 합니다.
        변경 하지 않을 값은 아예 입력하지 않아야 합니다. 
        ex) date만 변경할 경우, content는 입력하지 않습니다. -> "date": "2022-12-31" 만 입력
        
        To-Do List의 id와 수정된 할 일의 내용, 수정 시간이 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "toDoId", description = "상태를 변경할 To-Do List의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/to-do/{toDoId}/update")
    public ApiResponse<ToDoListUpdateResponseDTO> updateToDoList(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistToDoList Long toDoId,
        @RequestBody @Valid ToDoListRequestDTO.ToDoListCreateDTO request) {
        ToDoListUpdateResponseDTO toDoListUpdateResponseDTO = memberStudyCommandService.updateToDoList(
            studyId, toDoId, request);
        return ApiResponse.onSuccess(SuccessStatus._TO_DO_LIST_UPDATED, toDoListUpdateResponseDTO);
    }



    @Tag(name = "To-Do List")
    @Operation(summary = "[To-Do List] To-Do List 체크 처리", description = """ 
        ## [To-Do List] To-Do List에 작성한 할 일의 체크 상태를 변경 합니다.
        
        체크 표시 되어 있는 경우, 해당 API를 재호출 하면 체크가 해제됩니다.
        
        To-Do List의 id와 체크한 할 일의 id, 체크 여부가 반환됩니다.
       
        본인이 작성한 To-Do List만 체크할 수 있습니다.
        체크 여부가 true 인 경우, 할 일이 완료 되었음을 의미합니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "toDoId", description = "상태를 변경할 To-Do List의 id를 입력합니다.", required = true)
    @PostMapping("/studies/{studyId}/to-do/{toDoId}/check")
    public ApiResponse<ToDoListUpdateResponseDTO> checkToDoList(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistToDoList Long toDoId) {
        ToDoListUpdateResponseDTO toDoListUpdateResponseDTO = memberStudyCommandService.checkToDoList(
            studyId, toDoId);
        return ApiResponse.onSuccess(SuccessStatus._TO_DO_LIST_UPDATED, toDoListUpdateResponseDTO);
    }

    @Tag(name = "To-Do List")
    @Operation(summary = "[To-Do List] To-Do List 삭제", description = """ 
        ## [To-do list] 로그인한 회원이 참여하는 스터디에 To-Do List를 삭제합니다.
        
        To-Do List 완료 처리와는 다른 개념으로, To-Do List를 삭제합니다.
        To-Do List의 id와 상태 업데이트 시간이 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "toDoId", description = "삭제할 To-Do List의 id를 입력합니다.", required = true)
    @DeleteMapping("/studies/{studyId}/to-do/{toDoId}")
    public ApiResponse<ToDoListUpdateResponseDTO> deleteToDoList(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistToDoList Long toDoId) {
        ToDoListUpdateResponseDTO toDoListUpdateResponseDTO = memberStudyCommandService.deleteToDoList(
            studyId, toDoId);
        return ApiResponse.onSuccess(SuccessStatus._TO_DO_LIST_DELETED, toDoListUpdateResponseDTO);
    }

    @Tag(name = "To-Do List")
    @Operation(summary = "[To-Do List] 내 To-Do List 조회", description = """ 
        ## [To-Do List] 특정 스터디에 저장된 내 To-Do List를 날짜 별로 페이징 조회합니다.
        조회하고 싶은 날짜를 입력 받아, 해당 날짜의 할 일 목록, 체크 여부가 반환됩니다.
       
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "date", description = "조회할 날짜를 입력 받습니다. 날짜는 yyyy-MM-dd 형식으로 입력 받습니다.", required = true)
    @GetMapping("/studies/{studyId}/to-do/my")
    public ApiResponse<ToDoListSearchResponseDTO> getMyToDoList(
        @PathVariable @ExistStudy Long studyId,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam LocalDate date) {
        ToDoListSearchResponseDTO toDoList = memberStudyQueryService.getToDoList(studyId, date,
            PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._TO_DO_LIST_FOUND, toDoList);
    }

    @Tag(name = "To-Do List")
    @Operation(summary = "[To-Do List] 다른 스터디 원 To-Do List 조회", description = """ 
        ## [To-Do List] 특정 스터디에 저장된 다른 스터디원의 To-Do List를 날짜 별로 페이징 조회합니다.
        조회하고 싶은 날짜를 입력 받아, 해당 날짜의 할 일 목록, 체크 여부가 반환됩니다.
        """)
    @Parameter(name = "studyId", description = "스터디의 id를 입력합니다.", required = true)
    @Parameter(name = "memberId", description = "To-do list를 조회할 회원의 id를 입력합니다.", required = true)
    @Parameter(name = "page", description = "조회할 페이지 번호를 입력 받습니다. 페이지 번호는 0부터 시작합니다.", required = true)
    @Parameter(name = "size", description = "조회할 페이지 크기를 입력 받습니다. 페이지 크기는 1 이상의 정수 입니다. ", required = true)
    @Parameter(name = "date", description = "조회할 날짜를 입력 받습니다. 날짜는 yyyy-MM-dd 형식으로 입력 받습니다.", required = true)
    @GetMapping("/studies/{studyId}/to-do/members/{memberId}")
    public ApiResponse<ToDoListSearchResponseDTO> getOtherToDoList(
        @PathVariable @ExistStudy Long studyId,
        @PathVariable @ExistMember Long memberId,
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size,
        @RequestParam LocalDate date) {
        ToDoListSearchResponseDTO toDoList = memberStudyQueryService.getMemberToDoList(studyId,
            memberId, date, PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._TO_DO_LIST_FOUND, toDoList);
    }

}

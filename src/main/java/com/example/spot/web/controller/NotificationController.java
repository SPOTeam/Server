package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.web.dto.notification.NotificationRequestDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import com.example.spot.service.notification.NotificationCommandService;
import com.example.spot.service.notification.NotificationQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림 - 개발 중", description = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    //알림 전체 조회
    @Tag(name = "알림", description = "알림 관련 API")
    @Operation(summary = "[알림 전체 조회]", description = """
            ## [알림 전체 조회] 내게 할당된 알림 전체 조회
            내게 할당된 알림 전체를 조회합니다.
            """)
    @GetMapping("/members/{memberId}/notifications")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getAllNotifications(@PathVariable Long memberId) {
        List<NotificationResponseDTO.NotificationDTO> notificationDTO = notificationQueryService.getAllNotifications(memberId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);
    }

    //신청한 스터디 참여 확인 알림
    @Tag(name = "알림", description = "알림 관련 API")
    @Operation(summary = "[참가 신청한 스터디 알림 조회]", description = "유저가 참가 신청한 스터디 조회")
    @GetMapping("/members/{memberId}/notifications/applied-study")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getAppliedStudyNotification(@PathVariable Long memberId) {
        List<NotificationResponseDTO.NotificationDTO> notificationDTO = notificationQueryService.getAllAppliedStudyNotification(memberId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);

    }

    //알림 읽음 처리
    @Operation(summary = "[알림 읽음 처리 - 개발중]", description = """
            ## [알림 읽음 처리] 알림을 읽음 처리합니다.
            알림을 읽음 처리합니다.
            """)
    @PostMapping("/members/{memberId}/notifications/{notificationId}/read")
    public ApiResponse<NotificationResponseDTO.NotificationDTO> readNotification(@PathVariable Long memberId, @PathVariable Long notificationId) {
        NotificationResponseDTO.NotificationDTO notificationDTO = notificationCommandService.readNotification(memberId, notificationId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_READ, notificationDTO);

    }


    @Operation(summary = "[참가 신청한 스터디 참여 여부 - 개발중]", description = "유저가 참가 신청한 스터디 참여 여부")
    @GetMapping("/members/{memberId}/notifications/applied-study/{studyId}")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> checkAppliedStudyNotification(@PathVariable Long memberId, @PathVariable Long studyId) {
        return null;
    }

    // 신청한 스터디 참여
    @Operation(summary = "[참가 신청한 스터디 참여 처리  - 개발중]", description = """
            ## [참가 신청한 스터디 참여 확인] 유저가 참가 신청한 스터디를 참여 처리합니다.
            해당 스터디 참석 처리
            """)
    @PostMapping("/members/{memberId}/notifications/applied-study/{studyId}/join")
    public ApiResponse<NotificationResponseDTO.NotificationDTO> joinAppliedStudy(@PathVariable Long memberId, @PathVariable Long studyId,
                                                                                 @RequestBody NotificationRequestDTO.joinStudyDTO joinAppliedStudyRequestDTO) {
        NotificationResponseDTO.NotificationDTO notification = notificationCommandService.joinAppliedStudy(studyId, memberId, joinAppliedStudyRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_APPLIED_STUDY_JOINED, notification);
    }

    // 신청한 스터디 불참
    @Operation(summary = "[참가 신청한 스터디 불참 - 개발중]", description = """
            ## [참가 신청한 스터디 참여 취소] 유저가 참가 신청한 스터디를 불참 처리합니다.
            해당 스터디 참석 불참 처리
            """)
    @PostMapping("/members/{memberId}/notifications/applied-study/{studyId}/reject")
    public ApiResponse<NotificationResponseDTO.NotificationDTO> rejectAppliedStudy(@PathVariable Long memberId, @PathVariable Long studyId,
                                                                                   @RequestBody NotificationRequestDTO.rejectStudyDTO rejectAppliedStudyRequestDTO) {
        NotificationResponseDTO.NotificationDTO notification = notificationCommandService.rejectAppliedStudy(studyId, memberId, rejectAppliedStudyRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_APPLIED_STUDY_REJECTED, notification);
    }
}

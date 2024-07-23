package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.web.dto.notification.NotificationRequestDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import com.example.spot.web.service.notification.NotificationCommandService;
import com.example.spot.web.service.notification.NotificationQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "Notification API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    // 내게 할당된 알림 전체 조회
    @GetMapping("/notifications")
    @Operation(summary = "[알림 전체 조회 - 개발중]",
            description = """
                    ## [알림 전체 조회] 내게 할당된 알림 전체 조회
                    내게 할당된 알림 전체를 조회합니다.
                    """)
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getAllNotification(
            @RequestParam("userId") long userId,
            @RequestParam(value = "type", required = false) NotifyType type,
            @RequestParam(value = "status") String status,
            @RequestParam(value = "date") String date
    ) {
        List<NotificationResponseDTO.NotificationDTO> allNotifications = notificationQueryService.getAllNotifications(userId, type, status, date);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, allNotifications);
    }

    // 알림 읽음 처리
    @PostMapping("/notifications/{notificationId}/read")
    @Operation(summary = "[알림 읽음 처리 - 개발중]",
            description = """
                    ## [알림 읽음 처리] 알림을 읽음 처리합니다.
                    알림을 읽음 처리합니다.
                    """)
    public ApiResponse<NotificationResponseDTO.NotificationDTO> readNotification(
            @PathVariable("notificationId") Long notificationId,
            @RequestBody NotificationRequestDTO.readDTO notificationReadDTO) {
        NotificationResponseDTO.NotificationDTO notificationDTO = notificationCommandService.readNotification(notificationId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_READ, notificationDTO);
    }

    // 참가 신청한 스터디 최종 참여 확인 알림
    @GetMapping("/notifications/applied-study")
    @Operation(summary = "[참가 신청한 스터디 알림 조회 - 개발중]", description = "유저가 참가 신청한 스터디 조회")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getAppliedStudyNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("studyId") long studyId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "date", required = false) String date
    ) {
        List<NotificationResponseDTO.NotificationDTO> appliedStudyNotification = notificationQueryService.getAppliedStudyNotification(userId, studyId, status, date);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, appliedStudyNotification);
    }

    @GetMapping("/notifications/applied-study/{studyId}")
    @Operation(summary = "[참가 신청한 스터디 참여 여부 - 개발중]", description = "유저가 참가 신청한 스터디 참여 여부")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> checkAppliedStudyNotification(
            @PathVariable("notificationId") Long notificationId,
            @PathVariable("studyId") long studyId,
            @RequestParam("userId") Long userId,
            @RequestParam("status") String status,
            @RequestParam("date") String date
    ) {
        List<NotificationResponseDTO.NotificationDTO> notificationDTO = notificationQueryService.getAppliedStudyNotification(userId, studyId, status, date);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);
    }

    // 신청한 스터디 참여
    @PostMapping("/notifications/applied-study/{studyId}/join")
    @Operation(summary = "[참가 신청한 스터디 참여 처리  - 개발중]",
            description = """
                    ## [참가 신청한 스터디 참여 확인] 유저가 참가 신청한 스터디를 참여 처리합니다.
                    해당 스터디 참석 처리
                    """)
    public ApiResponse<NotificationResponseDTO.NotificationDTO> joinAppliedStudy(
            @PathVariable("studyId") long studyId,
            @RequestParam("userId") Long userId,
            @RequestParam("status") String status,
            @RequestParam("date") String date
    ) {
        NotificationResponseDTO.NotificationDTO notificationDTO = notificationCommandService.joinAppliedStudy(studyId, userId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_APPLIED_STUDY_CONFIRMED, notificationDTO);
    }

    // 신청한 스터디 불참
    @PostMapping("/notifications/applied-study/{notificationId}/{studyId}/reject")
    @Operation(summary = "[참가 신청한 스터디 불참 - 개발중]",
            description = """
                    ## [참가 신청한 스터디 참여 취소] 유저가 참가 신청한 스터디를 불참 처리합니다.
                    해당 스터디 참석 불참 처리
                    """)
    public ApiResponse<NotificationResponseDTO.NotificationDTO> rejectAppliedStudy(
            @PathVariable("notificationId") Long notificationId,
            @PathVariable("studyId") Long studyId,
            @RequestParam("userId") Long userId
    ) {
        NotificationResponseDTO.NotificationDTO notificationDTO = notificationCommandService.rejectAppliedStudy(studyId, userId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_APPLIED_STUDY_REJECTED, notificationDTO);
    }
}

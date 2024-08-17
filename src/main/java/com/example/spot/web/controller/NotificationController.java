package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.notification.NotificationRequestDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import com.example.spot.service.notification.NotificationCommandService;
import com.example.spot.service.notification.NotificationQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림", description = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    //알림 전체 조회
    @Operation(summary = "[내 알림 전체 조회]", description = """
            ## [내 알림 전체 조회] 
            내게 할당된 알림 전체를 조회합니다.
            """)
    @GetMapping("/notifications")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getAllNotifications() {
        List<NotificationResponseDTO.NotificationDTO> notificationDTO = notificationQueryService.getAllNotifications(
            SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);
    }

    //신청한 스터디 참여 확인 알림
    @Operation(summary = "[참가 신청한 스터디 알림 조회]", description = "유저가 참가 신청한 스터디 조회")
    @GetMapping("/notifications/applied-study")
    public ApiResponse<List<NotificationResponseDTO.NotificationDTO>> getAppliedStudyNotification() {
        List<NotificationResponseDTO.NotificationDTO> notificationDTO = notificationQueryService.getAllAppliedStudyNotification(
            SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);

    }

    //알림 읽음 처리
    @Operation(summary = "[알림 읽음 처리]", description = """
            ## [알림 읽음 처리] 알림을 읽음 처리합니다.
            알림을 읽음 처리합니다.
            """)
    @PostMapping("/notifications/{notificationId}/read")
    public ApiResponse<NotificationResponseDTO.NotificationDTO> readNotification(@PathVariable Long notificationId) {
        NotificationResponseDTO.NotificationDTO notificationDTO = notificationCommandService.readNotification(
            SecurityUtils.getCurrentUserId(), notificationId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_READ, notificationDTO);

    }

    // 신청한 스터디 참여
    @Operation(summary = "[참가 신청한 스터디 알람 처리  - 개발중]", description = """
            ## [참가 신청한 스터디 알람 처리] 유저가 참가 신청한 스터디에 대해 생성된 알림을 처리합니다.
            
            """)
    @PostMapping("/notifications/applied-study/{studyId}/join")
    public ApiResponse<NotificationResponseDTO.NotificationDTO> joinAppliedStudy( @PathVariable Long studyId,
                                                                                 @RequestBody NotificationRequestDTO.joinStudyDTO joinAppliedStudyRequestDTO) {
        NotificationResponseDTO.NotificationDTO notification = notificationCommandService.joinAppliedStudy(
            studyId, SecurityUtils.getCurrentUserId(), joinAppliedStudyRequestDTO);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_APPLIED_STUDY_JOINED, notification);
    }
}

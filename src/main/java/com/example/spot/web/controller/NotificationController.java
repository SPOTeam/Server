package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import com.example.spot.service.notification.NotificationCommandService;
import com.example.spot.service.notification.NotificationQueryService;

import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationProcessDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StudyNotificationListDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림", description = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/spot")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    //알림 전체 조회
    @Operation(summary = "[내 알림 전체 조회] 내게 생성된 알림을 전체 조회합니다.", description = """
            ## [내 알림 전체 조회] 내게 생성된 알림을 전체 조회합니다.
            
            알림의 내용, 생성 시간, 알림의 종류, 알림을 생성한 스터디의 이름을 반환합니다.
            
            알림의 종류는 다음과 같습니다.
            ANNOUNCEMENT, SCHEDULE_UPDATE ,TO_DO_UPDATE, POPULAR_POST
            
            """)
    @GetMapping("/notifications")
    public ApiResponse<NotificationListDTO> getAllNotifications(
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size
    ) {
        NotificationListDTO notificationDTO = notificationQueryService.getAllNotifications(
            SecurityUtils.getCurrentUserId(), PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);
    }


    @Operation(summary = "[참가 신청한 스터디 알림 조회] 참가 신청한 스터디에 대한 알림 조회", description = """
        
            ## [참가 신청한 스터디 알림 조회] 회원이 참가 신청한 스터디에 대한 알림을 조회합니다.
            
            참가 신청 했던 스터디의 제목과 프로필 이미지를 반환합니다.
            해당 알림은 본인의 스터디 신청을 스터디 소유자가 승인 처리 했을 경우 생성됩니다. 
            *참가 신청한 스터디 알람 처리* API를 통해 참여 버튼을 누르면 스터디에 참여할 수 있습니다.
            """)
    @GetMapping("/notifications/applied-study")
    public ApiResponse<StudyNotificationListDTO> getAppliedStudyNotification(
        @RequestParam @Min(0) Integer page,
        @RequestParam @Min(1) Integer size
    ) {
        StudyNotificationListDTO notificationDTO = notificationQueryService.getAllAppliedStudyNotification(
            SecurityUtils.getCurrentUserId(), PageRequest.of(page, size));
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);
    }

    //알림 읽음 처리
    @Operation(summary = "[알림 읽음 처리] 일반 알림 읽음 처리", description = """
            ## [알림 읽음 처리] 알림을 읽음 처리합니다.
            알림 처리 후 읽음 처리 결과를 반환합니다.
            """)
    @PostMapping("/notifications/{notificationId}/read")
    public ApiResponse<NotificationResponseDTO.NotificationProcessDTO> readNotification(@PathVariable Long notificationId) {
        NotificationResponseDTO.NotificationProcessDTO notificationDTO = notificationCommandService.readNotification(
            SecurityUtils.getCurrentUserId(), notificationId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_READ, notificationDTO);
    }

    // 신청한 스터디 참여
    @Operation(summary = "[참가 신청한 스터디 알람 처리] 알림 통한 스터디 참여 처리", description = """
            ## [참가 신청한 스터디 알람 처리] 유저가 참가 신청한 스터디에 대해 생성된 알림을 처리합니다.
            isAccepted 값이 true일 경우 스터디 참여, false일 경우 스터디 참여 거절 처리합니다.
            """)
    @PostMapping("/notifications/applied-study/{studyId}/join")
    public ApiResponse<NotificationProcessDTO> joinAppliedStudy(
        @PathVariable Long studyId,
        @RequestParam boolean isAccepted) {
        NotificationProcessDTO notification = notificationCommandService.joinAppliedStudy(
            studyId, SecurityUtils.getCurrentUserId(), isAccepted);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_APPLIED_STUDY_JOINED, notification);
    }
}

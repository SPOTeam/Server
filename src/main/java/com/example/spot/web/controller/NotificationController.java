package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import com.example.spot.web.service.notification.NotificationCommandService;
import com.example.spot.web.service.notification.NotificationQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @GetMapping("/notifications/")
    @Operation(summary = "[알림 전체 조회 - 개발중]",
            description = """
                    ## [알림 전체 조회] 내게 할당된 알림 전체 조회
                    내게 할당된 알림 전체를 조회합니다.
                    """)
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "type", description = "조회할 알림의 타입을 입력 받습니다.", required = true)
    @Parameter(name = "status", description = "조회할 알림의 상태를 입력 받습니다.", required = true)
    @Parameter(name = "date", description = "조회할 알림의 날짜를 입력 받습니다.", required = true)
    public ApiResponse<List<NotificationResponseDTO>> getAllNotification(
            @RequestParam("userId") long userId,
            @RequestParam(value = "type", required = false) NotifyType type,
            @RequestParam(value = "status") String status,
            @RequestParam(value = "date") String date
    ) {
        List<NotificationResponseDTO> allNotifications = notificationQueryService.getAllNotifications(userId, type, status, date);
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
            @PathVariable("notificationId") Long notificationId
    ) {
        NotificationResponseDTO.NotificationDTO notificationDTO = notificationCommandService.readNotification(notificationId);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, notificationDTO);
    }


    // 참가 신청한 스터디 최종 참여 확인 알림
    @GetMapping("/notifications/applied-study")
    @Operation(summary = "[참가 신청한 스터디 알림 조회 - 개발중]", description = "유저가 참가 신청한 스터디 조회")
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "studyId", description = "조회할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "status", description = "조회할 알림의 상태를 입력 받습니다.", required = false)
    @Parameter(name = "date", description = "조회할 알림의 날짜를 입력 받습니다.", required = false)
    public ApiResponse<List<NotificationResponseDTO>> getAppliedStudyNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("studyId") long studyId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "date", required = false) String date
    ) {
        List<NotificationResponseDTO> appliedStudyNotification = notificationQueryService.getAppliedStudyNotification(userId, studyId, status, date);
        return ApiResponse.onSuccess(SuccessStatus._NOTIFICATION_FOUND, appliedStudyNotification);
    }

    @GetMapping("/notifications/applied-study/{studyId}")
    @Operation(summary = "[참가 신청한 스터디 참여 여부 - 개발중]", description = "유저가 참가 신청한 스터디 참여 여부")
    @Parameter(name = "studyId", description = "조회할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    public ApiResponse<Object> checkAppliedStudyNotification(
            @PathVariable("studyId") long studyId,
            @RequestParam("userId") Long userId
    ) {
        return notificationCommandService.confirmAppliedStudyNotification(studyId, userId);
    }

    // 신청한 스터디 참여
    @PostMapping("/notifications/applied-study/{studyId}/confirm")
    @Operation(summary = "[참가 신청한 스터디 참여 처리  - 개발중]",
            description = """
                    ## [참가 신청한 스터디 참여 확인] 유저가 참가 신청한 스터디를 참여 처리합니다.
                    해당 스터디 참석 처리
                    """)
    @Parameter(name = "studyId", description = "조회할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "status", description = "조회할 알림의 상태를 입력 받습니다.", required = true)
    @Parameter(name = "date", description = "조회할 알림의 날짜를 입력 받습니다.", required = true)
    public void joinAppliedStudy(
            @PathVariable("studyId") long studyId,
            @RequestParam("userId") Long userId,
            @RequestParam("status") String status,
            @RequestParam("date") String date
    ) {
        return notificationCommandService.confirmAppliedStudy(studyId, userId, status, date);
    }

    // 신청한 스터디 불참
    @PostMapping("/notifications/applied-study/{studyId}/reject")
    @Operation(summary = "[참가 신청한 스터디 불참 - 개발중]",
            description = """
                    ## [참가 신청한 스터디 참여 취소] 유저가 참가 신청한 스터디를 불참 처리합니다.
                    해당 스터디 참석 불참 처리
                    """)
    @Parameter(name = "studyId", description = "조회할 스터디의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "userId", description = "조회할 유저의 ID를 입력 받습니다.", required = true)
    @Parameter(name = "status", description = "조회할 알림의 상태를 입력 받습니다.", required = true)
    @Parameter(name = "date", description = "조회할 알림의 날짜를 입력 받습니다.", required = true)
    public void rejectAppliedStudy(
            @PathVariable("studyId") Long studyId,
            @RequestParam("userId") Long userId,
            @RequestParam("status") String status,
            @RequestParam("date") String date
    ) {
        return null;
    }

    // 알림 존재 유무(알림 있으면 알림 아이콘 옆 붉은 점 표시)
    @GetMapping("/notifications/exist")
    @Operation(summary = "[알림 존재 유무 - 개발중]", description = "알림 존재 유무 조회(붉은 점 표시)")
    public boolean existNotification(
            @RequestParam("userId") Long userId
    ) {
        return true;
    }

    // 알림 확인 유무
    @GetMapping("/notifications/check")
    @Operation(summary = "[알림 확인 유무 - 개발중]", description = "알림 확인 유무 조회")
    public boolean checkNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("type") String type,
            @RequestParam("status") String status,
            @RequestParam("date") String date
    ) {
        return true;
    }
}

package com.example.spot.web.controller;

import com.example.spot.domain.enums.NotifyType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "Notification API")
@RestController
@RequestMapping("/spot")
public class NotificationController {

    //TODO: NotificationService 구현
    //private final NotificationService notificationService;

    //TODO: NotificationService 생성자 주입
    //public NotificationController(NotificationService notificationService) {}

    // 내게 할당된 알림 전체 조회
    @GetMapping("/notifications/")
    @Operation(summary = "[알림 전체 조회 - 개발중]", description = "유저에게 할당된 알림 전체를 조회")
    public void getAllNotification(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "type", required = false) NotifyType type,
            @RequestParam(value = "status") String status,
            @RequestParam(value = "date") String date
            ) {
    }

    // 참가 신청한 스터디 최종 참여 확인 알림
    @GetMapping("/notifications/applied-study")
    @Operation(summary = "[참가 신청한 스터디 알림 조회 - 개발중]", description = "유저가 참가 신청한 스터디 조회")
    public void getAppliedStudyNotification(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "date", required = false) String date
            ) {
    }

    @GetMapping("/notifications/applied-study/{studyId}")
    @Operation(summary = "[참가 신청한 스터디 참여 여부 - 개발중]", description = "유저가 참가 신청한 스터디 참여 여부")
    public void checkAppliedStudyNotification(
            @PathVariable("studyId") Long studyId,
            @RequestParam("userId") Long userId
            ) {
    }

    // 알림 존재 유무(알림 있으면 알림 아이콘 옆 붉은 점 표시)
    @GetMapping("/notifications/exist")
    @Operation(summary = "[알림 존재 유무 - 개발중]", description = "알림 존재 유무 조회(붉은 점 표시)")
    public void existNotification(
            @RequestParam("userId") Long userId
            ) {
    }

    // 알림 확인 유무
    @GetMapping("/notifications/check")
    @Operation(summary = "[알림 확인 유무 - 개발중]", description = "알림 확인 유무 조회")
    public void checkNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("type") String type,
            @RequestParam("status") String status,
            @RequestParam("date") String date
            ) {
    }

    // 알림 읽음 처리
}

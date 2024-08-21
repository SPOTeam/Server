package com.example.spot.domain.enums;

public enum ApplicationStatus {
    APPLIED, // 신청 승인 대기
    APPROVED, // 신청 승인 완료
    AWAITING_SELF_APPROVAL, // 본인 승인 대기
    REJECTED, // 신청 거절
}

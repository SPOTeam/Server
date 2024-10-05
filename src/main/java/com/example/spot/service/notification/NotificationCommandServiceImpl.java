package com.example.spot.service.notification;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationProcessDTO;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final MemberStudyRepository memberStudyRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 알림을 읽음 처리 합니다. 이미 읽은 알림인 경우 예외를 발생시킵니다.
     * @param memberId 사용자 ID
     * @param notificationId 알림 ID
     * @return 알림 읽음 처리 결과 및 처리 일시
     * @throws GeneralException 알림이 존재하지 않거나 이미 읽음 처리된 경우
     * @throws GeneralException 알림이 사용자에게 속하지 않는 경우
     * @throws GeneralException 알림이 이미 읽음 처리된 경우
     * @see NotificationProcessDTO
     */
    @Override
    public NotificationProcessDTO readNotification(Long memberId, Long notificationId) {

        // 알림 조회
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
            () -> new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND));

        // 알림이 사용자에게 속하지 않는 경우
        if (!Objects.equals(notification.getMember().getId(), memberId))
            throw new GeneralException(ErrorStatus._NOTIFICATION_IS_NOT_BELONG_TO_MEMBER);

        // 이미 읽음 처리된 경우
        if (notification.getIsChecked())
            throw new GeneralException(ErrorStatus._NOTIFICATION_ALREADY_READ);

        // 알림 읽음 처리
        notification.markAsRead();

        // 알림 읽음 처리 결과 반환
        return NotificationProcessDTO.builder()
            .isAccept(notification.getIsChecked())
            .processedAt(notification.getUpdatedAt())
            .build();
    }

    /**
     * 스터디 신청 알림에 대한 처리를 수행합니다. isAccept가 true인 경우 스터디 신청을 수락하고, false인 경우 거절합니다.
     * @param studyId 스터디 ID
     * @param memberId 사용자 ID
     * @param isAccept 스터디 신청 수락 여부
     * @return 스터디 신청 처리 결과 및 처리 일시
     * @throws GeneralException 알림이 존재하지 않거나 이미 읽음 처리된 경우
     * @throws GeneralException 스터디 신청자가 존재하지 않는 경우
     * @see NotificationProcessDTO
     */
    @Override
    public NotificationProcessDTO joinAppliedStudy(Long studyId, Long memberId, boolean isAccept) {

        // 스터디 신청 알림 조회 -
        Notification notification = notificationRepository.findByMemberIdAndStudyIdAndTypeAndIsChecked(
            memberId, studyId, NotifyType.STUDY_APPLY, false).orElseThrow(() -> new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND));

        // 이미 읽음 처리된 경우
        if (notification.getIsChecked())
            throw new GeneralException(ErrorStatus._NOTIFICATION_ALREADY_READ);

        // 스터디 신청자 조회
        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(
            memberId, studyId, ApplicationStatus.AWAITING_SELF_APPROVAL).orElseThrow(
            () -> new GeneralException(ErrorStatus._STUDY_APPLICANT_NOT_FOUND));

        // 스터디 신청 처리
        if (isAccept) {
            // 스터디 신청 수락
            memberStudy.setStatus(ApplicationStatus.APPROVED);
        }else {
            // 스터디 신청 거절
            memberStudy.setStatus(ApplicationStatus.REJECTED);
        }
        // 알림 읽음 처리
        notification.markAsRead();

        // 스터디 신청 처리 결과 반환
        return NotificationProcessDTO.builder()
            .isAccept(isAccept)
            .processedAt(notification.getUpdatedAt())
            .build();
    }

}

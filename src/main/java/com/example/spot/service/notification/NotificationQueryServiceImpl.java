package com.example.spot.service.notification;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO.NotificationDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StduyNotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StduyNotificationListDTO.StudyNotificationDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    /**
     * 회원이 참가 신청한 스터디에 대한 알림을 조회합니다.
     * @param memberId 사용자 ID
     * @param pageable 페이징 정보
     * @return 참가 신청한 스터디에 대한 알림 목록
     * @throws GeneralException 알림이 존재하지 않는 경우
     */
    @Override
    public StduyNotificationListDTO getAllAppliedStudyNotification(Long memberId, Pageable pageable) {
        // 특정 회원이 참가 신청한 스터디에 대한 알림 조회
        List<Notification> notifications = notificationRepository.findByMemberIdAndTypeAndIsChecked(
            memberId, pageable, NotifyType.STUDY_APPLY, false);

        // 알림이 존재하지 않는 경우
        if (notifications.isEmpty())
            throw new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND);

        // DTO를 담을 리스트 생성
        List<StudyNotificationDTO> notificationDTOs = new ArrayList<>();

        // 알림을 DTO로 변환하여 리스트에 추가
        notifications.forEach(notification -> {
            StudyNotificationDTO notificationDTO = StudyNotificationDTO.builder()
                .notificationId(notification.getId())
                .studyId(notification.getStudy().getId())
                .createdAt(notification.getCreatedAt())
                .type(notification.getType())
                .studyTitle(notification.getStudy().getTitle())
                .studyProfileImage(notification.getStudy().getProfileImage())
                .isChecked(notification.getIsChecked())
                .build();
            notificationDTOs.add(notificationDTO);
        });

        // DTO 리스트를 반환
        return StduyNotificationListDTO.builder()
            .notifications(notificationDTOs)
            .totalNotificationCount((long) notificationDTOs.size())
            .uncheckedNotificationCount((long) (int) notificationDTOs.stream().filter(notificationDTO -> !notificationDTO.getIsChecked()).count())
            .build();
    }

    /**
     * 회원에게 생성된 알림을 전체 조회합니다.
     * @param memberId 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 목록
     * @throws GeneralException 알림이 존재하지 않는 경우
     */
    @Override
    public NotificationListDTO getAllNotifications(Long memberId, Pageable pageable) {

        // 특정 회원에게 생성된 알림을 조회
        List<Notification> notifications = notificationRepository.findByMemberIdAndTypeNot(
            memberId, pageable, NotifyType.STUDY_APPLY);

        // 알림이 존재하지 않는 경우
        if (notifications.isEmpty())
            throw new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND);

        // DTO를 담을 리스트 생성
        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        // 알림을 DTO로 변환하여 리스트에 추가
        notifications.forEach(notification -> {
            NotificationDTO notificationDTO = NotificationDTO.builder()
                .notificationId(notification.getId())
                .createdAt(notification.getCreatedAt())
                .type(notification.getType())
                .studyTitle(notification.getStudy().getTitle())
                .notifierName(notification.getNotifierName()) // 알림 생성한 회원 이름
                .isChecked(notification.getIsChecked())
                .build();
            notificationDTOs.add(notificationDTO);
        });

        // DTO 리스트를 반환
        return NotificationListDTO.builder()
            .notifications(notificationDTOs)
            .totalNotificationCount((long) notificationDTOs.size())
            .uncheckedNotificationCount((long) (int) notificationDTOs.stream().filter(notificationDTO -> !notificationDTO.getIsChecked()).count())
            .build();
    }



}

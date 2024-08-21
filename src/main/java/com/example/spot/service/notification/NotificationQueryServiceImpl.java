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


    // 신청한 스터디 알림 전체 조회
    @Override
    public StduyNotificationListDTO getAllAppliedStudyNotification(Long memberId, Pageable pageable) {
        List<Notification> notifications = notificationRepository.findByMemberIdAndTypeAndIsChecked(
            memberId, pageable, NotifyType.STUDY_APPLY, false);

        if (notifications.isEmpty())
            throw new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND);

        List<StudyNotificationDTO> notificationDTOs = new ArrayList<>();

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

        return StduyNotificationListDTO.builder()
            .notifications(notificationDTOs)
            .totalNotificationCount((long) notificationDTOs.size())
            .uncheckedNotificationCount((long) (int) notificationDTOs.stream().filter(notificationDTO -> !notificationDTO.getIsChecked()).count())
            .build();
    }

    // 생성된 알림 전체 조회
    @Override
    public NotificationListDTO getAllNotifications(Long memberId, Pageable pageable) {

        List<Notification> notifications = notificationRepository.findByMemberIdAndTypeNot(
            memberId, pageable, NotifyType.STUDY_APPLY);

        if (notifications.isEmpty())
            throw new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND);

        List<NotificationDTO> notificationDTOs = new ArrayList<>();

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


        return NotificationListDTO.builder()
            .notifications(notificationDTOs)
            .totalNotificationCount((long) notificationDTOs.size())
            .uncheckedNotificationCount((long) (int) notificationDTOs.stream().filter(notificationDTO -> !notificationDTO.getIsChecked()).count())
            .build();
    }



}

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


    @Override
    public NotificationProcessDTO readNotification(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
            () -> new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND));

        if (!Objects.equals(notification.getMember().getId(), memberId))
            throw new GeneralException(ErrorStatus._NOTIFICATION_IS_NOT_BELONG_TO_MEMBER);

        if (notification.getIsChecked())
            throw new GeneralException(ErrorStatus._NOTIFICATION_ALREADY_READ);

        notification.markAsRead();

        return NotificationProcessDTO.builder()
            .isAccept(notification.getIsChecked())
            .processedAt(notification.getUpdatedAt())
            .build();
    }

    @Override
    public NotificationProcessDTO joinAppliedStudy(Long studyId, Long memberId, boolean isAccept) {

        Notification notification = notificationRepository.findByMemberIdAndStudyIdAndType(
            memberId, studyId, NotifyType.STUDY_APPLY).orElseThrow(() -> new GeneralException(ErrorStatus._NOTIFICATION_NOT_FOUND));

        if (notification.getIsChecked())
            throw new GeneralException(ErrorStatus._NOTIFICATION_ALREADY_READ);

        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(
            memberId, studyId, ApplicationStatus.AWAITING_SELF_APPROVAL).orElseThrow(
            () -> new GeneralException(ErrorStatus._STUDY_APPLICANT_NOT_FOUND));

        if (isAccept) {
            memberStudy.setStatus(ApplicationStatus.APPROVED);
        }else {
            memberStudy.setStatus(ApplicationStatus.REJECTED);
        }
        notification.markAsRead();

        return NotificationProcessDTO.builder()
            .isAccept(isAccept)
            .processedAt(notification.getUpdatedAt())
            .build();
    }

}

package com.example.spot.service.notification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.NotificationHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationDTO;

@Service
@RequiredArgsConstructor
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final MemberStudyRepository memberStudyRepository;

    @Override
    public List<NotificationDTO> getAllNotifications(Long memberId) {

        //예외 처리
        if (!notificationRepository.existsById(memberId)) {
            throw new NotificationHandler(ErrorStatus._NOTIFICATION_NOT_FOUND);
        }

        return notificationRepository.findByMemberId(memberId)
                .stream().map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getAllAppliedStudyNotification(Long memberId) {

        // 참여 신청 스터디 조회
        List<MemberStudy> appliedStudies = memberStudyRepository.findAllByMemberIdAndStatus(memberId, ApplicationStatus.APPLIED);
        if (appliedStudies.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_NOT_FOUND);
        }
        // 침여 신청 스터디 조회
        return notificationRepository.findAppliedStudyByMemberId(memberId)
                .stream().map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean exitsNotification(Long memberId) {
        return notificationRepository.existsById(memberId);
    }
}

package com.example.spot.service.notification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.NotificationHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final StudyRepository studyRepository;

    @Override
    public NotificationListDTO getAllNotifications(Long memberId, Pageable pageable) {
        
        if(!memberRepository.existsById(memberId)) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);
        }
        Page<Notification> notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        return NotificationListDTO.of(notifications);
    }

    
    @Override
    public NotificationListDTO getAllAppliedStudyNotification(Long memberId, Pageable pageable) {
        // 메서드 구현 예정
        if(!memberRepository.existsById(memberId)) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);
        }

        List<MemberStudy> memberStudies = memberStudyRepository.findAllByMemberIdAndStatus(memberId, ApplicationStatus.APPLIED);

        List<Long> studyIds = memberStudies.stream().map(ms->ms.getStudy().getId()).collect(Collectors.toList());

        Page<Notification> notifications = notificationRepository.findAllByMemberIdAndStudyIdIn(memberId, studyIds, pageable);

        return NotificationListDTO.of(notifications);
    }

    @Override
    public NotificationDTO getNotification(Long memberId, Long notificationId) {

        Notification notification = notificationRepository.findByIdAndMemberId(notificationId, memberId)
        .orElseThrow(()->new NotificationHandler(ErrorStatus._NOTIFICATION_NOT_FOUND));

        return NotificationResponseDTO.NotificationDTO.from(notification);
    }

    @Override
    public NotificationDTO getAppliedStudyNotification(Long memberId, Long studyId) {

        return null;

    }
    
    @Override
    public boolean existsUnreadNotification(Long memberId) {

        if(!memberRepository.existsById(memberId)) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);
        }

        return notificationRepository.existsByMemberIdAndIsReadFalse(memberId);
    }


}

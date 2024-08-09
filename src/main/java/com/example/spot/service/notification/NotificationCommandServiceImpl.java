package com.example.spot.service.notification;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.NotificationHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO;

@Service
@RequiredArgsConstructor
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final MemberStudyRepository memberStudyRepository;

    @Override
    public NotificationResponseDTO.NotificationDTO readNotification(Long memberId, Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationHandler(ErrorStatus._NOTIFICATION_NOT_FOUND));

        if (!notification.getMember().getId().equals(memberId)) {
            throw new NotificationHandler(ErrorStatus._NOTIFICATION_NOT_FOUND);
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        return NotificationResponseDTO.NotificationDTO.from(notification);
    }

    @Override
    public NotificationResponseDTO.NotificationDTO joinAppliedStudy(long studyId, Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPLIED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        if (memberStudy.getStatus() != ApplicationStatus.APPLIED) {
            throw new StudyHandler(ErrorStatus._APPLIED_STUDY_NOT_FOUND);
        }

        memberStudy.setStatus(ApplicationStatus.APPROVED);
        memberStudyRepository.save(memberStudy);

        Notification notification = Notification.builder()
                .title("참여 완료")
                .content("참여가 완료되었습니다! 내 스터디에서 스터디 일정을 확인해주세요.")
                .isChecked(true)
                .member(member)
                .build();

        notification = notificationRepository.save(notification);

        return NotificationResponseDTO.NotificationDTO.from(notification);
    }

    @Override
    public NotificationResponseDTO.NotificationDTO rejectAppliedStudy(long studyId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        MemberStudy memberStudy = memberStudyRepository.findByMemberIdAndStudyIdAndStatus(memberId, studyId, ApplicationStatus.APPLIED)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._APPLIED_STUDY_NOT_FOUND));

        memberStudy.setStatus(ApplicationStatus.CANCELED);
        memberStudyRepository.save(memberStudy);

        Notification notification = Notification.builder()
                .title("불참 완료")
                .content("")
                .isChecked(true)
                .member(member)
                .build();

        return NotificationResponseDTO.NotificationDTO.from(notification);
    }
}

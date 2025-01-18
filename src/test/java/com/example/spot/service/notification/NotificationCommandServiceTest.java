package com.example.spot.service.notification;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;

import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.Member;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationProcessDTO;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationCommandServiceTest {

    @InjectMocks
    private NotificationCommandServiceImpl notificationCommandService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberStudyRepository memberStudyRepository;

    @Mock
    private Member member;

    @Mock
    private Study study;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

    private MemberStudy memberStudy;

    @BeforeEach
    void init() {
        this.notification1 = Notification.builder()
                .id(1L).study(study).member(member).type(NotifyType.STUDY_APPLY).notifierName("Test")
                .isChecked(false).build();
        this.notification2 = Notification.builder()
                .id(2L).study(study).member(member).type(NotifyType.ANNOUNCEMENT).notifierName("Test")
                .isChecked(false).build();
        this.notification2 = Notification.builder()
                .id(3L).study(study).member(member).type(NotifyType.STUDY_APPLY).notifierName("Test")
                .isChecked(true).build();

        this.memberStudy = MemberStudy.builder().status(ApplicationStatus.APPLIED).build();
    }

    /* --------------------------------- 알림 읽음 처리 ----------------------------------- */

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void 알림_읽음_처리_성공() {
        // given
        given(member.getId()).willReturn(1L);

        when(notificationRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(notification1));

        // when
        NotificationProcessDTO response = notificationCommandService.readNotification(1L, 1L);

        // then
        assertEquals(true, response.isAccept());
    }

    @Test
    @DisplayName("알림이 없는 경우")
    void 알림이_없는_경우() {
        // given
        when(notificationRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(GeneralException.class, () -> {
            notificationCommandService.readNotification(1L, 1L);
        });
    }

    @Test
    @DisplayName("알림이 사용자에게 속하지 않는 경우")
    void 알림이_사용자에게_속하지_않는_경우() {
        // given
        given(member.getId()).willReturn(1L);

        when(notificationRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(notification1));

        // when & then
        assertThrows(GeneralException.class, () -> {
            notificationCommandService.readNotification(2L, 1L);
        });
    }

    @Test
    @DisplayName("이미 읽음 처리된 경우")
    void 이미_읽음_처리_된_경우() {
        // given
        given(member.getId()).willReturn(1L);

        when(notificationRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(notification3));

        // when & then
        assertThrows(GeneralException.class, () -> {
            notificationCommandService.readNotification(1L, 1L);
        });
    }

    /* --------------------------------- 스터디 알림 처리 ----------------------------------- */

    @Test
    @DisplayName("스터디 신청 처리 성공")
    void 스터디_신청_처리_성공() {
        // given
        when(notificationRepository.findByMemberIdAndStudyIdAndTypeAndIsChecked(
                anyLong(), anyLong(), any(), anyBoolean()
        )).thenReturn(Optional.ofNullable(notification1));

        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(
                anyLong(), anyLong(), any()
        )).thenReturn(Optional.ofNullable(memberStudy));

        // when
        NotificationProcessDTO response =
                notificationCommandService.joinAppliedStudy(1L, 1L, true);

        // then
        assertEquals(true, response.isAccept());
    }

    @Test
    @DisplayName("스터디 신청 알림이 이미 처리 된 경우")
    void 스터디_알림이_이미_읽음_처리_된_경우() {
        // given
        when(notificationRepository.findByMemberIdAndStudyIdAndTypeAndIsChecked(
                anyLong(), anyLong(), any(), anyBoolean()
        )).thenReturn(Optional.ofNullable(notification3));

        // when & then
        assertThrows(GeneralException.class, () -> {
            notificationCommandService.joinAppliedStudy(1L, 1L, true);
        });
    }

    @Test
    @DisplayName("스터디 신청 알림이 없는 경우")
    void 스터디_신청이_없는_경우() {
        // given
        when(notificationRepository.findByMemberIdAndStudyIdAndTypeAndIsChecked(
                anyLong(), anyLong(), any(), anyBoolean()
        )).thenReturn(Optional.ofNullable(notification1));

        when(memberStudyRepository.findByMemberIdAndStudyIdAndStatus(
                anyLong(), anyLong(), any()
        )).thenReturn(Optional.empty());

        // when & then
        assertThrows(GeneralException.class, () -> {
            notificationCommandService.joinAppliedStudy(1L, 1L, true);
        });
    }
}

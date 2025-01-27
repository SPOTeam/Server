package com.example.spot.service.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.Member;
import com.example.spot.domain.Notification;
import com.example.spot.domain.enums.NotifyType;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.NotificationRepository;
import com.example.spot.web.dto.notification.NotificationResponseDTO.NotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StduyNotificationListDTO;
import com.example.spot.web.dto.notification.NotificationResponseDTO.StduyNotificationListDTO.StudyNotificationDTO;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
class NotificationQueryServiceTest {

    @InjectMocks
    private NotificationQueryServiceImpl notificationQueryService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private Member member;

    @Mock
    private Study study;

    @Mock
    private Pageable pageable;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

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
    }


    /*---------------------- 스터디 알림 조회 ---------------------- */

    @Test
    @DisplayName("스터디 알림 조회 성공")
    void 스터디_알림_조회_성공() {
        // given
        given(notification1.getStudy().getId()).willReturn(1L);
        given(notification1.getStudy().getTitle()).willReturn("스터디 참여");
        given(notification1.getStudy().getProfileImage()).willReturn("img");

        when(notificationRepository.findByMemberIdAndTypeAndIsChecked(
                anyLong(), any(), any(), anyBoolean()
        )).thenReturn(List.of(notification1));


        // when
        StduyNotificationListDTO response = notificationQueryService.getAllAppliedStudyNotification(1L, pageable);

        // then
        assertEquals(1L, response.getTotalNotificationCount());
        assertEquals(1L, response.getNotifications().get(0).getNotificationId());
    }

    @Test
    @DisplayName("참여 신청한 스터디 알림이 없는 경우")
    void 스터디_알림이_없는_경우() {
        // given
        when(notificationRepository.findByMemberIdAndTypeAndIsChecked(
                anyLong(), any(), any(), anyBoolean()
        )).thenReturn(List.of());

        // when & then
        assertThrows(GeneralException.class, () -> {
            notificationQueryService.getAllAppliedStudyNotification(
                        1L, pageable);
        });
    }

    /*---------------------- 알림 전체 조회 ---------------------- */

    @Test
    @DisplayName("전체 알림 조회 성공")
    void 전체_알림_조회_성공() {
        // given
        given(notification1.getStudy().getTitle()).willReturn("스터디 참여");

        when(notificationRepository.findByMemberIdAndTypeNot(
                anyLong(), any(), any()
        )).thenReturn(List.of(notification1, notification2));


        // when
        NotificationListDTO response = notificationQueryService.getAllNotifications(1L, pageable);

        // then
        assertEquals(2L, response.getTotalNotificationCount());
        assertEquals(1L, response.getNotifications().get(0).getNotificationId());
    }

    @Test
    @DisplayName("조회할 알림이 없는 경우")
    void 조회할_알림이_없는_경우() {
        // given

        when(notificationRepository.findByMemberIdAndTypeNot(
                anyLong(), any(), any()
        )).thenReturn(List.of());

        // when & then
        assertThrows(GeneralException.class, () -> {
            notificationQueryService.getAllNotifications(
                    1L, pageable);
        });
    }

}

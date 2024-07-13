package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Carrier;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Carrier carrier;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false)
    private LocalDate birth;

    @Column
    private LocalDateTime inactive;

    @Column(nullable = false)
    private Boolean personalInfo;

    @Column(nullable = false)
    private Boolean idInfo;

    //== 스터디 희망사유 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<StudyReason> studyReasonList;

    //== 알림 ==//
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notificationList;
}

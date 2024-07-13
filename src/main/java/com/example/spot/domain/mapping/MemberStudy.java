package com.example.spot.domain.mapping;

import com.example.spot.domain.Member;
import com.example.spot.domain.Study;
import com.example.spot.domain.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class MemberStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    //== 회원 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //== 스터디 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;
}
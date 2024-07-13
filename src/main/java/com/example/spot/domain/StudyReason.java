package com.example.spot.domain;

import com.example.spot.domain.enums.Reason;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class StudyReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column
    private Reason reason;

    //== 회원 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}

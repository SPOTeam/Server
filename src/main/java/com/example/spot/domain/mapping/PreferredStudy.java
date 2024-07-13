package com.example.spot.domain.mapping;

import com.example.spot.domain.Member;
import com.example.spot.domain.Study;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class PreferredStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    //== 회원 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //== 스터디 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;
}

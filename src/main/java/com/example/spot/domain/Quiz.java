package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.mapping.MemberAttendance;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, length = 50)
    private String question;

    @Column(nullable = false, length = 50)
    private String answer;

    //== 출석 회원 목록 ==//
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<MemberAttendance> memberAttendanceList;

    //== 해당 퀴즈를 생성한 스터디 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;
}

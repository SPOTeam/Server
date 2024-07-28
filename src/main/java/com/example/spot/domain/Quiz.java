package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.mapping.MemberAttendance;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public Quiz(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.memberAttendanceList = new ArrayList<>();
    }

/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addMemberAttendance(MemberAttendance memberAttendance) {
        memberAttendanceList.add(memberAttendance);
        memberAttendance.setQuiz(this);
    }

    public void deleteMemberAttendance(MemberAttendance memberAttendance) {
        memberAttendanceList.remove(memberAttendance);
        memberAttendance.setQuiz(null);
    }
}

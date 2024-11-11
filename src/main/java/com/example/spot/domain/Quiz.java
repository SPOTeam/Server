package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.mapping.MemberAttendance;
import com.example.spot.domain.study.Schedule;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, length = 20)
    private String question;

    @Column(nullable = false, length = 10)
    private String answer;

    //== 출석 회원 목록 ==//
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<MemberAttendance> memberAttendanceList;

    //== 해당 퀴즈를 생성한 일정 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    //== 퀴즈 생성자 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column
    private LocalDateTime createdAt;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedAt;

/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public Quiz(Schedule schedule, Member member, String question, String answer, LocalDateTime createdAt) {
        this.schedule = schedule;
        this.member = member;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
        this.memberAttendanceList = new ArrayList<>();
    }

/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addMemberAttendance(MemberAttendance memberAttendance) {
        memberAttendanceList.add(memberAttendance);
        memberAttendance.setQuiz(this);
    }

    public void deleteMemberAttendance(MemberAttendance memberAttendance) {
        memberAttendanceList.remove(memberAttendance);
    }
}

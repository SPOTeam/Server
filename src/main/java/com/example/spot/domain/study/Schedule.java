package com.example.spot.domain.study;
import com.example.spot.domain.Member;
import com.example.spot.domain.Quiz;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Period;
import com.example.spot.web.dto.memberstudy.request.ScheduleRequestDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Schedule extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<Quiz> quizList = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false, length = 20)
    private String location;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime finishedAt;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isAllDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public Schedule(Study study, Member member, String title, String location,
                    LocalDateTime staredAt, LocalDateTime finishedAt,
                    Boolean isAllDay, Period period) {
        this.study = study;
        this.member = member;
        this.title = title;
        this.location = location;
        this.startedAt = staredAt;
        this.finishedAt = finishedAt;
        this.isAllDay = isAllDay;
        this.period = period;
        this.quizList = new ArrayList<>();
    }

/* ----------------------------- 메소드 ------------------------------------- */


    public void addQuiz(Quiz quiz) {
        quizList.add(quiz);
        quiz.setSchedule(this);
    }
    public void modSchedule(ScheduleRequestDTO.ScheduleDTO scheduleDTO) {
        this.title = scheduleDTO.getTitle();
        this.location = scheduleDTO.getLocation();
        this.startedAt = scheduleDTO.getStartedAt();
        this.finishedAt = scheduleDTO.getFinishedAt();
        this.isAllDay = scheduleDTO.getIsAllDay();
        this.period = scheduleDTO.getPeriod();
    }

}

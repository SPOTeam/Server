package com.example.spot.domain.study;

import com.example.spot.domain.Notification;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.PreferredStudy;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Integer minAge;

    @Column(nullable = false)
    private Integer maxAge;

    @Column(nullable = false)
    private boolean hasFee;

    @Column(nullable = false)
    private Integer fee;

    @Column(nullable = false)
    private String profileImage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyState studyState;

    @Column(length = 30)
    private String performance;

    @Column(nullable = false)
    private Boolean isOnline;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer heartCount;

    @Column(nullable = false)
    private String goal;

    @Column(nullable = false)
    private String introduction;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long hitNum;

    @Column(nullable = false)
    private Long maxPeople;

    private LocalDateTime finishedAt;

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyPost> posts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyTheme> studyThemes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<MemberStudy> memberStudies = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<RegionStudy> regionStudies = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<PreferredStudy> preferredStudies = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyPost> studyPosts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<ToDoList> toDoLists = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addMemberStudy(MemberStudy memberStudy) {
        memberStudies.add(memberStudy);
        memberStudy.setStudy(this);
    }

    public void addRegionStudy(RegionStudy regionStudy) {
        regionStudies.add(regionStudy);
        regionStudy.setStudy(this);
    }

    public void addStudyTheme(StudyTheme studyTheme) {
        studyThemes.add(studyTheme);
        studyTheme.setStudy(this);
    }

    public void addPreferredStudy(PreferredStudy preferredStudy) {
        preferredStudies.add(preferredStudy);
        preferredStudy.changeStudy(this);
        this.heartCount++;
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.setStudy(this);
    }

    public void addVote(Vote vote) {
        votes.add(vote);
        vote.setStudy(this);
    }

    public void updateSchedule(Schedule schedule) {
        schedules.set(schedules.indexOf(schedule), schedule);
    }

    public void addStudyPost(StudyPost studyPost) {
        if (this.studyPosts == null) {
            this.studyPosts = new ArrayList<>();
        }
        this.studyPosts.add(studyPost);
        studyPost.setStudy(this);
    }

    public void updateStudyPost(StudyPost studyPost) {
        studyPosts.set(studyPosts.indexOf(studyPost), studyPost);
    }

    public void deleteStudyPost(StudyPost studyPost) {
        studyPosts.remove(studyPost);
    }

    // preferredStudy 삭제
    public void deletePreferredStudy(PreferredStudy preferredStudy) {
        this.heartCount--;
    }

    // hit 증가
    public void increaseHit() {
        this.hitNum++;
    }

    public void updateVote(Vote vote) {
        votes.set(votes.indexOf(vote), vote);
    }

    public void deleteVote(Vote vote) {
        votes.remove(vote);
    }

    public void addToDoList(ToDoList toDoList) {
        toDoLists.add(toDoList);
    }

    public void terminateStudy(String performance) {
        this.studyState = StudyState.COMPLETED;
        this.status = Status.OFF;
        this.performance = performance;
        this.finishedAt = LocalDateTime.now();
    }

    public void updateStudyInfo(
            String title, String introduction, String goal, Boolean isOnline, Boolean hasFee, Integer fee, Integer minAge, Integer maxAge,
            Gender gender, Long maxPeople, String profileImage) {
        this.title = title;
        this.introduction = introduction;
        this.goal = goal;
        this.isOnline = isOnline;
        this.hasFee = hasFee;
        this.fee = fee;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.gender = gender;
        this.maxPeople = maxPeople;
        this.profileImage = profileImage;
    }
}

package com.example.spot.domain.study;

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

    @Column(nullable = false)
    private Boolean isOnline;

    @Column(nullable = false)
    private Integer heartCount;

    @Column(nullable = false)
    private String goal;

    @Column(nullable = false)
    private String introduction;

    @Column(nullable = false)
    private String title;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Long hitNum;

    @Column(nullable = false)
    private Long maxPeople;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StudyPost> posts = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StudyTheme> studyThemes = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MemberStudy> memberStudies = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RegionStudy> regionStudies = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PreferredStudy> preferredStudies = new ArrayList<>();



/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public Study(Gender gender, Integer minAge, Integer maxAge, Integer fee,
                 String profileImage, boolean hasFee,
                 Boolean isOnline, String goal, String introduction,
                 String title, Long maxPeople) {
        this.gender = gender;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.fee = fee;
        this.profileImage = profileImage;
        this.studyState = StudyState.RECRUITING;
        this.isOnline = isOnline;
        this.heartCount = 0;
        this.hasFee = hasFee;
        this.goal = goal;
        this.introduction = introduction;
        this.title = title;
        this.status = Status.ON;
        this.hitNum = 0L;
        this.maxPeople = maxPeople;
        this.schedules = new ArrayList<>();
        this.posts = new ArrayList<>();
        this.votes = new ArrayList<>();
        this.studyThemes = new ArrayList<>();
        this.preferredStudies = new ArrayList<>();
        this.memberStudies = new ArrayList<>();
        this.regionStudies = new ArrayList<>();

    }

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

    // preferredStudy 삭제
    public void deletePreferredStudy(PreferredStudy preferredStudy) {
        this.heartCount--;
    }

    // hit 증가
    public void increaseHit() {
        this.hitNum++;
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.setStudy(this);
    }

    public void updateSchedule(Schedule schedule) {
        schedules.set(schedules.indexOf(schedule), schedule);
    }
}

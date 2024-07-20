package com.example.spot.domain.study;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.StudyState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Study extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Integer minAge;

    @Column(nullable = false)
    private Integer maxAge;

    @Column(nullable = false)
    private Integer fee;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyState studyState;

    @Column(nullable = false)
    private boolean isOnline;

    @Column(nullable = false)
    private int heartCount;

    @Column(nullable = false)
    private String goal;

    @Column(nullable = false)
    private String introduction;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Long hitNum;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<StudyPost> posts;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<Vote> votes;

}

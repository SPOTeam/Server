package com.example.domain;

import com.example.domain.common.BaseEntity;
import com.example.domain.enums.Gender;
import com.example.domain.enums.Status;
import com.example.domain.enums.StudyState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DialectOverride;

@Entity
@Data
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
    private Status status;



}

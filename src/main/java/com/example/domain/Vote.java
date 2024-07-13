package com.example.domain;

import com.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
public class Vote extends BaseEntity {
    @Id @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Study study;

    //private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean isMultipleChoice;

    @Column(nullable = false)
    private LocalDateTime finishedAt;

}

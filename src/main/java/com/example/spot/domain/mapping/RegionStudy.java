package com.example.spot.domain.mapping;

import com.example.spot.domain.Region;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
public class RegionStudy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

/* ----------------------------- 생성자 ------------------------------------- */

    protected RegionStudy() {}

    @Builder
    public RegionStudy(Region region, Study study) {
        this.region = region;
        this.study = study;
    }
}

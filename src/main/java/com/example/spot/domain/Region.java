package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.PreferredRegion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Region extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String province;

    private String district;

    private String neighborhood;

    @Builder.Default
    @OneToMany(mappedBy = "region")
    private List<RegionStudy> regionStudyList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "region")
    private List<PreferredRegion> prefferedRegionList = new ArrayList<>();


/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addRegionStudy(RegionStudy regionStudy) {
        regionStudyList.add(regionStudy);
        regionStudy.setRegion(this);
    }

    public void addPreferredRegion(PreferredRegion preferredRegion) {
        prefferedRegionList.add(preferredRegion);
        preferredRegion.setRegion(this);
    }
}

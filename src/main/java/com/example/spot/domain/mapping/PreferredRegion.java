package com.example.spot.domain.mapping;

import com.example.spot.domain.Region;
import com.example.spot.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class PreferredRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

}

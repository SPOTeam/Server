package com.example.spot.domain.study;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.mapping.MemberVote;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "options")
public class Option extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    private String content;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL)
    private List<MemberVote> memberVotes;

/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public Option(Vote vote, String content) {
        this.vote = vote;
        this.content = content;
        this.memberVotes = new ArrayList<>();
    }

/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addMemberVote(MemberVote memberVote) {
        if (memberVotes == null) {
            memberVotes = new ArrayList<>();
        }
        memberVotes.add(memberVote);
        memberVote.setOption(this);
    }
}

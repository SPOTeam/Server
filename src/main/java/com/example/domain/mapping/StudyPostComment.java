package com.example.domain.mapping;

import com.example.domain.common.BaseEntity;
import com.example.domain.mapping.StudyLikedComment;
import com.example.domain.mapping.StudyPost;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class StudyPostComment extends BaseEntity {

    @Id @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private StudyPost studyPost;

    // @Column(nullable = false)
    // private Member member;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer likeCount;

    @OneToMany(mappedBy = "studyPostComment", cascade = CascadeType.ALL)
    private List<StudyLikedComment> likedComments;





}

package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostScheduleComments extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer rank;
    private String title;
    private Integer commentCount;

    private PostScheduleComments(Integer rank, String title, Integer commentCount) {
        this.rank = rank;
        this.title = title;
        this.commentCount = commentCount;
    }

    public static PostScheduleComments of(Post post, Integer rank) {
        int commentSize = post.getPostCommentList().size();
        return new PostScheduleComments(rank, post.getTitle(), commentSize);
    }
}

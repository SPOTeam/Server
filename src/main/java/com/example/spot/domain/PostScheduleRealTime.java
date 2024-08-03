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
public class PostScheduleRealTime extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer ranking;
    private String title;
    private Integer commentCount;
    private Integer likeCount;
    private Integer hitCount;

    private PostScheduleRealTime(Integer rank, String title, Integer commentCount, Integer likeCount, Integer hitCount) {
        this.ranking = rank;
        this.title = title;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.hitCount = hitCount;
    }

    public static PostScheduleRealTime of(Post post, Integer rank) {
        int commentSize = post.getPostCommentList().size();
        int likeSize = post.getLikedPostList().size();
        int hitSize = post.getHitNum();
        return new PostScheduleRealTime(rank, post.getTitle(), commentSize, likeSize, hitSize);
    }
}

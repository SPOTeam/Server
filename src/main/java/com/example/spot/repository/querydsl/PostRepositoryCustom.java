package com.example.spot.repository.querydsl;

import com.example.spot.domain.Post;

import java.util.List;

public interface PostRepositoryCustom {

    // 댓글 수를 기준으로 게시글을 정렬하여 가져오는 쿼리 메서드
    List<Post> findTopByOrderByCommentCountDesc();

    // 좋아요 수를 기준으로 게시글을 정렬하여 가져오는 쿼리 메서드
    List<Post> findTopByOrderByLikeNumDesc();

    // 좋아요 수, 조회수, 댓글 수를 합산하여 가장 높은 다섯개의 게시글을 가져오는 쿼리 메서드
    List<Post> findTopByRealTimeScore();

    //게시글 타입별 최신 게시글 출력
    List<Post> findRepresentativePosts();

}

package com.example.spot.repository.querydsl;

import com.example.spot.domain.Post;

import java.util.List;

public interface PostRepositoryCustom {

    // 댓글 수를 기준으로 게시글을 정렬하여 가져오는 쿼리 메서드
    List<Post> findTopByOrderByCommentCountDesc();
}

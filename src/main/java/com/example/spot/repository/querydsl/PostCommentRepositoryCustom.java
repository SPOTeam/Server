package com.example.spot.repository.querydsl;

import com.example.spot.domain.PostComment;

import java.util.List;

public interface PostCommentRepositoryCustom {
    List<PostComment> findCommentsByPostId(Long postId);
}


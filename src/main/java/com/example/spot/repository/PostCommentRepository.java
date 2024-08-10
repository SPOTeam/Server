package com.example.spot.repository;

import com.example.spot.domain.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostId(Long postId);
}

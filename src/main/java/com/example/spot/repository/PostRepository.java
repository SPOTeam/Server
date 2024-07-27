package com.example.spot.repository;

import com.example.spot.domain.Post;
import com.example.spot.domain.enums.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardAndPostReportListIsEmpty(Board board, Pageable pageable);

}

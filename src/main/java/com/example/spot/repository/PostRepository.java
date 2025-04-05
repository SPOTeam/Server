package com.example.spot.repository;

import com.example.spot.domain.Post;
import com.example.spot.domain.enums.Board;
import com.example.spot.repository.querydsl.PostRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    // 정렬
     Page<Post> findByBoardAndPostReportListIsEmptyOrderByCreatedAtDesc(Board board, Pageable pageable);
    Page<Post> findByBoardAndPostReportListIsEmpty(Board board, Pageable pageable);

    // 정렬 조건 필요
    Page<Post> findByPostReportListIsEmptyOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByPostReportListIsEmpty(Pageable pageable); // 모든 게시글 조회

}

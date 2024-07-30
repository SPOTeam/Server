package com.example.spot.repository;

import com.example.spot.domain.mapping.StudyLikedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyLikedPostRepository extends JpaRepository<StudyLikedPost, Long> {
}

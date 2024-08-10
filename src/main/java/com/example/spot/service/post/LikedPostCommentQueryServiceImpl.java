package com.example.spot.service.post;

import com.example.spot.repository.LikedPostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikedPostCommentQueryServiceImpl implements LikedPostCommentQueryService{
    private final LikedPostCommentRepository likedPostCommentRepository;

    @Override
    public long countByPostCommentIdAndIsLikedTrue(Long postCommentId){
        return likedPostCommentRepository.countByPostCommentIdAndIsLikedTrue(postCommentId);
    }

}

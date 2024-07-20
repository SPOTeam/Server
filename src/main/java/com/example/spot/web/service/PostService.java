package com.example.spot.web.service;

import com.example.spot.web.dto.post.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    public void createPost(PostCreateRequest postCreateRequest) {
        // validate & save
    }

    public void deletePost(Long postId) {
        // validate & delete
    }

    public PostSingleResponse getSinglePost(Long postId) {
        // validate & read
        return null;
    }

    public PostPagingResponse postTypePaging(String type, Pageable pageable) {
        // validate & read
        return null;
    }

    public PostBest5Response getPostBest5(String sortType) {
        // validate & read
        return null;
    }

    public PostHomeResponse getPostHome(String sortType){
        return null;
    }
}

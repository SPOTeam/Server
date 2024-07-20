package com.example.spot.web.dto.post;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostHomeResponse {

    private PostBest5Response postBest5Response;

    private List<PostBoard5Response> postBoard5Responses;

    //공지
}

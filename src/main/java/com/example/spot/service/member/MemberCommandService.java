package com.example.spot.service.member;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface MemberCommandService {

    void signUpByKAKAO(String code) throws JsonProcessingException;

}

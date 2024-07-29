package com.example.spot.service.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;

public interface MemberCommandService {

    void signUpByKAKAO(String code) throws JsonProcessingException;

    void redirectURL() throws IOException;

}

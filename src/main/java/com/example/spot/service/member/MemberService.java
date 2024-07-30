package com.example.spot.service.member;

import com.example.spot.domain.Member;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {

    MemberResponseDTO.MemberSignInDTO signUpByKAKAO(String code) throws JsonProcessingException;

    MemberResponseDTO.MemberSignInDTO signUpByKAKAOForTest(String code) throws JsonProcessingException;

    void redirectURL() throws IOException;

    Member findMemberByEmail(String email);

    boolean isMemberExists(String email);

}

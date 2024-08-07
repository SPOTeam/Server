package com.example.spot.service.member;


import com.example.spot.domain.Member;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberInfoListDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberRegionDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberThemeDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.TestMemberDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {
    // 테스트 용 멤버 생성
    MemberResponseDTO.MemberTestDTO testMember(TestMemberDTO requestDTO);

    MemberResponseDTO.MemberSignInDTO signUpByKAKAO(String code) throws JsonProcessingException;

    MemberResponseDTO.MemberSignInDTO signUpByKAKAOForTest(String code)
        throws JsonProcessingException;

    void redirectURL() throws IOException;

    Member findMemberByEmail(String email);

    boolean isMemberExists(String email);

    MemberResponseDTO.MemberUpdateDTO updateTheme(Long memberId, MemberThemeDTO requestDTO);
    MemberResponseDTO.MemberUpdateDTO updateRegion(Long memberId, MemberRegionDTO requestDTO);
    MemberResponseDTO.MemberUpdateDTO updateProfile(Long memberId, MemberInfoListDTO requestDTO);
    MemberResponseDTO.MemberUpdateDTO toAdmin(Long memberId);
}


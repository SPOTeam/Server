package com.example.spot.service.member;


import com.example.spot.domain.Member;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberInfoListDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberReasonDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberRegionDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberThemeDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberUpdateDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

public interface MemberService extends UserDetailsService {
    // 테스트 용 멤버 생성
    MemberResponseDTO.MemberTestDTO testMember(MemberInfoListDTO memberInfoListDTO);

    MemberResponseDTO.SocialLoginSignInDTO signUpByKAKAO(String code) throws JsonProcessingException;

    MemberResponseDTO.SocialLoginSignInDTO signUpByKAKAOForTest(String code)
        throws JsonProcessingException;

    void redirectURL() throws IOException;

    Member findMemberByEmail(String email);

    boolean isMemberExists(String email);

    MemberResponseDTO.MemberUpdateDTO updateTheme(Long memberId, MemberThemeDTO requestDTO);
    MemberResponseDTO.MemberUpdateDTO updateRegion(Long memberId, MemberRegionDTO requestDTO);
    MemberResponseDTO.MemberUpdateDTO updateProfile(Long memberId, MemberUpdateDTO requestDTO);
    MemberResponseDTO.MemberUpdateDTO updateStudyReason(Long memberId, MemberReasonDTO requestDTO);

    MemberResponseDTO.MemberThemeDTO getThemes(Long memberId);
    MemberResponseDTO.MemberRegionDTO getRegions(Long memberId);
    MemberResponseDTO.MemberStudyReasonDTO getStudyReasons(Long memberId);
    MemberResponseDTO.MemberUpdateDTO toAdmin(Long memberId);

    String getNickname(Long memberId);

    @Transactional
    void save(Member member);
}


package com.example.spot.service.admin;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.admin.AdminResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

/* ----------------------------- 회원 정보 관리 API ------------------------------------- */

    @Override
    public boolean getIsAdmin() {
        Long memberId = SecurityUtils.getCurrentUserId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        return member.getIsAdmin();
    }

    @Override
    public AdminResponseDTO.DeletedMemberListDTO deleteInactiveMembers() {

        // 삭제 기준일시
        LocalDateTime stdTime = LocalDateTime.now().minusDays(30);

        // 회원 삭제
        List<Member> deletedMembers = memberRepository.findAllByInactiveBefore(stdTime);
        List<Long> deletedMemberIds = deletedMembers.stream().map(Member::getId).toList();
        AdminResponseDTO.DeletedMemberListDTO deletedMemberListDTO = AdminResponseDTO.DeletedMemberListDTO.toDTO(deletedMembers);

        // Token 정리
        refreshTokenRepository.deleteAllByMemberIdIn(deletedMemberIds);

        // 회원 정보 정리
        memberRepository.deleteAllByIdInBatch(deletedMemberIds);

        return deletedMemberListDTO;
    }

/* ----------------------------- 신고 내역 관리 API ------------------------------------- */

}

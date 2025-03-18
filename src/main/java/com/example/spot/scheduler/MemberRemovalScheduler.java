package com.example.spot.scheduler;

import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.service.admin.AdminService;
import com.example.spot.web.dto.admin.AdminResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberRemovalScheduler {

    private final AdminService adminService;

    // 매일 오전 6시에 탈퇴 후 30일이 지난 회원을 삭제합니다.
    @Scheduled(cron = "0 0 6 * * *")
    public void deleteMembers() {
        AdminResponseDTO.DeletedMemberListDTO deletedMemberListDTO = adminService.deleteInactiveMembers();
        log.info("deleted Members : {}", deletedMemberListDTO.toString());
    }
}

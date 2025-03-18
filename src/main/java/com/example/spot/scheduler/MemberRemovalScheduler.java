package com.example.spot.scheduler;

import com.example.spot.service.admin.AdminService;
import com.example.spot.web.dto.admin.AdminResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("Deleted Members:  {}", deletedMemberListDTO.getDeletedMembers().size());
        deletedMemberListDTO.getDeletedMembers().forEach(member ->
                log.info("Deleted Member: id={}, email={}", member.getMemberId(), member.getEmail())
        );
    }
}

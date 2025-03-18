package com.example.spot.service.admin;

import com.example.spot.web.dto.admin.AdminResponseDTO;

public interface AdminService {

/* ----------------------------- 회원 정보 관리 API ------------------------------------- */

    boolean getIsAdmin();

    AdminResponseDTO.DeletedMemberListDTO deleteInactiveMembers();

/* ----------------------------- 신고 내역 관리 API ------------------------------------- */

}

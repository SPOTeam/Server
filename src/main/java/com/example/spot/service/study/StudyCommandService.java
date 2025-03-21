package com.example.spot.service.study;

import com.example.spot.web.dto.study.request.StudyJoinRequestDTO;
import com.example.spot.web.dto.study.request.StudyRegisterRequestDTO;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;
import com.example.spot.web.dto.study.response.StudyJoinResponseDTO;
import com.example.spot.web.dto.study.response.StudyLikeResponseDTO;
import com.example.spot.web.dto.study.response.StudyRegisterResponseDTO;

public interface StudyCommandService {

    StudyJoinResponseDTO.JoinDTO applyToStudy(Long studyId, StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO);

    StudyRegisterResponseDTO.RegisterDTO registerStudy(StudyRegisterRequestDTO.RegisterDTO studyRegisterRequestDTO);

    StudyLikeResponseDTO likeStudy(Long memberId, Long studyId);

    void addHotKeyword(String keyword);

    // 스터디 정보 수정
    StudyRegisterResponseDTO.RegisterDTO updateStudyInfo(Long studyId, StudyRegisterRequestDTO.RegisterDTO studyInfoDTO);
}

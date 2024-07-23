package com.example.spot.service.study;

import com.example.spot.web.dto.study.request.StudyJoinRequestDTO;
import com.example.spot.web.dto.study.request.StudyRegisterRequestDTO;
import com.example.spot.web.dto.study.response.StudyJoinResponseDTO;
import com.example.spot.web.dto.study.response.StudyRegisterResponseDTO;

public interface StudyCommandService {

    StudyJoinResponseDTO.JoinDTO applyToStudy(Long memberId, Long studyId, StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO);

    StudyRegisterResponseDTO.RegisterDTO registerStudy(Long memberId, StudyRegisterRequestDTO.RegisterDTO studyRegisterRequestDTO);
}

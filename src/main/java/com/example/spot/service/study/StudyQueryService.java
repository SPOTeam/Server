package com.example.spot.service.study;

import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;

public interface StudyQueryService {

    public StudyInfoResponseDTO.StudyInfoDTO getStudyInfo(Long studyId);
}

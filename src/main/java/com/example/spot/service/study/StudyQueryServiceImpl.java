package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyQueryServiceImpl implements StudyQueryService {

    private final MemberRepository memberRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final StudyRepository studyRepository;

/* ----------------------------- 스터디 생성/참여 관련 API ------------------------------------- */

    public StudyInfoResponseDTO.StudyInfoDTO getStudyInfo(Long studyId) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        List<MemberStudy> memberStudyList = study.getMemberStudies().stream()
                .filter(MemberStudy::getIsOwned)
                .toList();

        if (memberStudyList.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_OWNER_NOT_FOUND);
        }

        Member owner = memberStudyList.get(0).getMember();
        return StudyInfoResponseDTO.StudyInfoDTO.toDTO(study, owner);
    }
}

package com.example.spot.web.dto.study.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyMemberResponseDTO {
    private long totalElements;
    private List<StudyMemberDTO> members;

    public StudyMemberResponseDTO(List<StudyMemberDTO> members){
        this.totalElements = members.size();
        this.members = members;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyMemberDTO{
        private Long memberId;
        private String nickname;
        private String profileImage;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyApplyMemberDTO{
        private Long memberId;
        private Long studyId;
        private String nickname;
        private String profileImage;
        private String introduction;
    }
}

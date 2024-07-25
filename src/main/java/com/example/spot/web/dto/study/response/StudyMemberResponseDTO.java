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
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int size;
    private List<StudyMemberDTO> members;

    public StudyMemberResponseDTO(Page<?> page, List<StudyMemberDTO> members, long totalElements){
        this.totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / page.getSize());
        this.totalElements = totalElements;
        this.first = page.isFirst();
        this.last = page.isLast();
        this.size = page.getSize();
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
}

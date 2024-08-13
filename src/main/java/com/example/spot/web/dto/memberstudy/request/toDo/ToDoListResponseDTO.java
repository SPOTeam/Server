package com.example.spot.web.dto.memberstudy.request.toDo;

import com.example.spot.web.dto.search.SearchResponseDTO;
import com.example.spot.web.dto.search.SearchResponseDTO.SearchStudyDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

public class ToDoListResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToDoListCreateResponseDTO {
        private Long id;
        private String content;
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToDoListUpdateResponseDTO {
        private Long id;
        private boolean isDone;
        private LocalDateTime updatedAt;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToDoListSearchResponseDTO {
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
        private int size;
        private List<ToDoListDTO> content;
        private int number;

        public ToDoListSearchResponseDTO(Page<?> page, List<ToDoListDTO> content , long totalElements) {
            this.totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / page.getSize());
            this.totalElements = totalElements;
            this.first = page.isFirst();
            this.last = page.isLast();
            this.size = page.getSize();
            this.content = content;
            this.number = page.getNumber();
        }

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ToDoListDTO{
            private Long id;
            private String content;
            private boolean isDone;
            private LocalDate date;
        }
    }

}

package com.example.spot.web.dto.memberstudy.request.toDo;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ToDoListRequestDTO {


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToDoListCreateDTO{
        private String content;
        private LocalDate date;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToDoListUpdateDTO{
        private String content;
    }


}

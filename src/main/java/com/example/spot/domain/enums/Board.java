package com.example.spot.domain.enums;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.PostHandler;
import lombok.Getter;

@Getter
public enum Board {
     ALL,
     PASS_EXPERIENCE,      // 합격후기
     INFORMATION_SHARING,  // 정보공유
     COUNSELING,           // 고민상담
     JOB_TALK,             // 취준토크
     FREE_TALK,            // 자유토크
     SPOT_ANNOUNCEMENT;     // SPOT공지

     public static Board findByValue(String inputBoardType) {
          for (Board board : Board.values()) {
               if (board.name().equals(inputBoardType)) {
                    return board;
               }
          }
          throw new PostHandler(ErrorStatus._INVALID_BOARD_TYPE);
     }
}

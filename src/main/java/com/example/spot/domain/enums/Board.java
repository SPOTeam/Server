package com.example.spot.domain.enums;

import lombok.Getter;

@Getter
public enum Board {
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
          // ToDo 추후에 통합에러로 수정
          throw new IllegalArgumentException("잘못된 게시글 타입요청");
     }
}

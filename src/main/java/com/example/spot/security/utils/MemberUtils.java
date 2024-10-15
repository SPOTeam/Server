package com.example.spot.security.utils;

import java.util.Random;

public class MemberUtils {

    /**
     * 랜덤한 휴대전화 번호를 생성합니다.
     * @return 랜덤한 휴대전화 번호
     */
    public static String generatePhoneNumber() {
        Random random = new Random();

        // 010으로 시작하는 한국의 일반적인 휴대전화 번호 형식
        StringBuilder phoneNumber = new StringBuilder("010-");

        // 4자리 숫자 생성
        for (int i = 0; i < 4; i++)
            phoneNumber.append(random.nextInt(10)); // 0부터 9까지의 숫자 중 랜덤 선택

        phoneNumber.append("-");

        // 4자리 숫자 생성
        for (int i = 0; i < 4; i++)
            phoneNumber.append(random.nextInt(10)); // 0부터 9까지의 숫자 중 랜덤 선택

        return phoneNumber.toString();
    }

}

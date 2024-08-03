package com.example.spot.validation.validator;

import com.example.spot.security.utils.JwtTokenProvider;

public class MemberValidator {
    private static JwtTokenProvider jwtTokenProvider;

    public static void validateMember (String token, Long memberId) {
        Long tokenUserId = jwtTokenProvider.getMemberIdByToken(token);
        if (!tokenUserId.equals(memberId)) {
            throw new SecurityException("User ID does not match");
        }
    }
}

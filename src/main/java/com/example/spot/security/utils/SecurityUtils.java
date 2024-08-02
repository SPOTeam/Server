package com.example.spot.security.utils;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        return Long.valueOf(authentication.getName());
    }

    public static void verifyUserId(Long memberId) {
        Long currentUserId = getCurrentUserId();
        if (!Objects.equals(currentUserId, memberId)) {
            throw new GeneralException(ErrorStatus._MEMBER_NO_ACCESS);
        }
    }

}

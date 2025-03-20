package com.example.spot.security.utils;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     * @return 현재 인증된 사용자의 ID
     */
    public static Long getCurrentUserId() {
        // 현재 인증된 사용자의 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 인증된 사용자가 없거나 인증되지 않은 경우 예외를 발생시킵니다.
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        // 인증된 사용자의 ID를 반환합니다.
        return Long.valueOf(authentication.getName());
    }

    /**
     * 현재 인증된 사용자의 ID와 매개변수로 전달된 ID가 일치하는지 확인합니다.
     * @param memberId 회원 ID
     */
    public static void verifyUserId(Long memberId) {
        // 관리자면, 모든 API 접근 가능
        if (isAdmin())
            return;
        // 현재 인증된 사용자의 ID를 가져옵니다.
        Long currentUserId = getCurrentUserId();

        // 현재 인증된 사용자의 ID와 매개변수로 전달된 ID가 일치하지 않는 경우 예외를 발생시킵니다.
        if (!Objects.equals(currentUserId, memberId))
            throw new GeneralException(ErrorStatus._MEMBER_NO_ACCESS);
    }

    /**
     * 현재 인증된 사용자의 역할이 ADMIN인지 확인합니다.
     * @return 현재 인증된 사용자의 역할이 ADMIN인지 여부
     */
    public static boolean isAdmin() {
        // 현재 인증된 사용자의 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자의 역할이 ADMIN인지 확인합니다.
        return authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }


    /**
     * 현재 인증된 사용자의 이메일을 반환합니다.
     * @return 현재 인증된 사용자의 이메일
     */
    public static String getVerifiedTempUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }
        return authentication.getName();
    }

    /**
     * 현재 인증된 사용자의 로그인 정보를 삭제합니다.
     * 로그인 정보를 삭제하며 SecurityContext도 함께 삭제합니다.
     */
    public static void deleteCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
    }
}

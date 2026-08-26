package com.surveyplus.creator.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CookieUtil {

    // 로그인 상태 유지를 선택했을 때 리프레시 토큰 쿠키의 만료 기한
    private static final long REMEMBER_ME_MAX_AGE = 7L * 24 * 60 * 60; // 7일

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    private HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    public void createRefreshTokenCookie(String refreshToken, boolean rememberMe) {

        HttpServletResponse response = getResponse();

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)       // 자바스크립트에서 쿠키 탈취 방지 (XSS 보호)
                .secure(cookieSecure) // HTTPS에서만 전송 (운영 환경에서는 true로 설정)
                .path("/")            // 모든 경로에서 쿠키 접근 가능
                .sameSite("Lax");  // CSRF 공격 방지 (동일 도메인에서만 전송)

        // 로그인 상태 유지를 선택했을 때만 만료 기한을 길게 설정 - 선택하지 않으면 maxAge를 지정하지 않아
        // 브라우저를 완전히 닫으면 사라지는 세션 쿠키로 발급됨(리프레시 토큰 JWT 자체 만료 기한도 TokenProvider에서 함께 짧게 발급)
        if (rememberMe) {
            cookieBuilder.maxAge(REMEMBER_ME_MAX_AGE);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }

    public void deleteRefreshTokenCookie() {

        HttpServletResponse response = getResponse();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
               .httpOnly(true)
               .secure(cookieSecure)
               .path("/")
               .sameSite("Lax")
               .maxAge(0) // 0으로 설정하면 즉시 삭제됨
               .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

package com.surveyplus.creator.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CookieUtil {

    private HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    public void createRefreshTokenCookie(String refreshToken) {

        HttpServletResponse response = getResponse();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)       // 자바스크립트에서 쿠키 탈취 방지 (XSS 보호)
                .secure(false)         // HTTPS에서만 전송 (운영 환경 필수)
                .path("/")            // 모든 경로에서 쿠키 접근 가능
                .sameSite("Lax")   // CSRF 공격 방지 (동일 도메인에서만 전송)
                .maxAge(7 * 24 * 60 * 60) // 7일 동안 유지
                .build();
        System.out.println("리프레쉬 토큰 발급 : " + refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteRefreshTokenCookie() {

        HttpServletResponse response = getResponse();

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
               .httpOnly(true)
               .secure(false)
               .path("/")
               .sameSite("Lax")
               .maxAge(0) // 0으로 설정하면 즉시 삭제됨
               .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

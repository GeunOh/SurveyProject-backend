package com.surveyplus.creator.auth.controller;

import com.surveyplus.creator.auth.service.AuthService;
import com.surveyplus.creator.global.exception.ApiResponse;
import com.surveyplus.creator.global.jwt.TokenErrorCode;
import com.surveyplus.creator.global.jwt.TokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        log.info("Access Token 재발급 요청");

        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = extractRefreshTokenFromCookie(request);

        log.info("Access Token 재발급 성공");

        return ResponseEntity.ok(ApiResponse.success(authService.reissueAccessToken(refreshToken)));
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) { // 쿠키 이름 확인
                    return cookie.getValue();
                }
            }
        }
        throw new TokenException(TokenErrorCode.INVALID_TOKEN);
    }
}
package com.surveyplus.creator.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
    // 로그인 상태 유지 여부 - true면 리프레시 토큰(쿠키)을 더 길게 발급
    private boolean rememberMe;
}

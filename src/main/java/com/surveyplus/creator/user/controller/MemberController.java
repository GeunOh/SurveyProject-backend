package com.surveyplus.creator.user.controller;

import com.surveyplus.creator.global.exception.ApiResponse;
import com.surveyplus.creator.user.dto.request.LoginRequest;
import com.surveyplus.creator.user.dto.request.SignUpRequest;
import com.surveyplus.creator.user.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입 메서드 (POST)
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("회원가입 요청 :{}", signUpRequest);

        return ResponseEntity.ok(ApiResponse.success(memberService.signUp(signUpRequest)));
    }

    // 2. 로그인 메서드 (POST)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("로그인 요청 :{}", loginRequest);

        return ResponseEntity.ok(ApiResponse.success(memberService.login(loginRequest)));
    }

    // 3. 로그아웃 메서드 (POST)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        memberService.logout();

        return ResponseEntity.ok(ApiResponse.success());
    }
}

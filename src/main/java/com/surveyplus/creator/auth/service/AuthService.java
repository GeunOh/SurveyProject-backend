package com.surveyplus.creator.auth.service;

import com.surveyplus.creator.global.jwt.TokenDto;
import com.surveyplus.creator.global.jwt.TokenMemberInfo;
import com.surveyplus.creator.global.jwt.TokenProvider;
import com.surveyplus.creator.user.entity.Member;
import com.surveyplus.creator.user.exception.MemberErrorCode;
import com.surveyplus.creator.user.exception.MemberException;
import com.surveyplus.creator.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository; // 회원 정보 조회를 위한 Repository

    @Transactional(readOnly = true)
    public TokenDto reissueAccessToken(String refreshToken) {
        log.info("Refresh Token 재발급 서비스 시작");

        // 1. Refresh Token 유효성 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            log.warn("유효하지 않거나 만료된 Refresh Token입니다.");
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token에서 Authentication 객체 추출
        Authentication authentication = tokenProvider.getAuthentication(refreshToken);
        String email = authentication.getName();

        // 3. DB에서 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 💡 4. 새로운 Access Token만 단독 생성
        String newAccessToken = tokenProvider.createAccessToken(authentication);
        long now = (new Date()).getTime();
        long accessTokenExpiresIn = now + (1000 * 60 * 30); // 30분

        // 5. 유저 정보 구성
        TokenMemberInfo tokenMemberInfo = TokenMemberInfo.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getNickname())
                .build();

        log.info("Access Token 및 유저 정보 재발급 완료: email={}", email);

        // 6. 새 액세스 토큰과 유저 정보를 담아 DTO 반환
        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .refreshToken(null) // 리프레시는 기존 쿠키 그대로 유지하므로 null 처리
                .tokenMemberInfo(tokenMemberInfo)
                .build();
    }
}

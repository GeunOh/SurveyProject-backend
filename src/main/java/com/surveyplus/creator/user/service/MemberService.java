package com.surveyplus.creator.user.service;

import com.surveyplus.creator.global.jwt.TokenDto;
import com.surveyplus.creator.global.jwt.TokenProvider;
import com.surveyplus.creator.global.util.CookieUtil;
import com.surveyplus.creator.user.dto.request.LoginRequest;
import com.surveyplus.creator.user.dto.request.SignUpRequest;
import com.surveyplus.creator.user.dto.response.MemberResponse;
import com.surveyplus.creator.user.entity.Member;
import com.surveyplus.creator.user.exception.MemberErrorCode;
import com.surveyplus.creator.user.exception.MemberException;
import com.surveyplus.creator.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;

    @Transactional
    public MemberResponse signUp(SignUpRequest signUpRequest) {
        
        //중복회원 검사
        if(memberRepository.existsByEmail(signUpRequest.getEmail())){
            throw new MemberException(MemberErrorCode.EMAIL_DUPLICATED);
        }

        Member member = signUpRequest.toEntity();

        // 3. 비밀번호 암호화
        if (signUpRequest.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
            member.updatePassword(encodedPassword); // Entity에 비밀번호 변경 메서드 필요
        }

        // 4. 저장
        Member savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    public TokenDto login(LoginRequest loginRequest) {
        // 1. 사용자 조회 (존재하지 않으면 예외 발생)
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. 비밀번호 일치 확인 (암호화된 비밀번호와 입력값 비교)
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // 3. JWT 토큰 발급
        // 실제 운영 환경에서는 AccessToken과 RefreshToken을 함께 발급합니다.
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication, member);

        cookieUtil.createRefreshTokenCookie(tokenDto.getRefreshToken());

        return tokenDto;
    }

    public void logout() {
        cookieUtil.deleteRefreshTokenCookie();
    }
}

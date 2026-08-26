package com.surveyplus.creator.global.jwt;

import com.surveyplus.creator.user.entity.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String MEMBER_ID_KEY = "memberId";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;            // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일 (기본)
    private static final long REFRESH_TOKEN_EXPIRE_TIME_REMEMBER = 1000L * 60 * 60 * 24 * 7; // 7일 (로그인 상태 유지)

    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
    
    //AccessToken, RefreshToken 생성
    public TokenDto generateTokenDto(Authentication authentication, Member member, boolean rememberMe) {

        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
            .subject(authentication.getName())       // payload "sub": "name"
            .claim(AUTHORITIES_KEY, authorities)        // payload "auth": "ROLE_USER"
            .claim(MEMBER_ID_KEY, member.getId())       // payload "memberId": 1 (ex)
            .expiration(accessTokenExpiresIn)        // payload "exp": 151621022 (ex)
            .signWith(key)    // header "alg": "HS512"
            .compact();

        // Refresh Token 생성 (로그인 상태 유지를 선택했으면 더 길게 만료)
        long refreshTokenExpireTime = rememberMe ? REFRESH_TOKEN_EXPIRE_TIME_REMEMBER : REFRESH_TOKEN_EXPIRE_TIME;
        String refreshToken = Jwts.builder()
            .subject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .claim(MEMBER_ID_KEY, member.getId())
            .expiration(new Date(now + refreshTokenExpireTime))
            .signWith(key)
            .compact();

        TokenMemberInfo tokenMemberInfo = TokenMemberInfo.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getNickname())
                .build();

        return TokenDto.builder()
            .grantType(BEARER_TYPE)
            .accessToken(accessToken)
            .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
            .refreshToken(refreshToken)
            .tokenMemberInfo(tokenMemberInfo)
            .build();
    }

    public String createAccessToken(Authentication authentication, Long memberId) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .claim(MEMBER_ID_KEY, memberId)
                .expiration(accessTokenExpiresIn)
                .signWith(key)
                .compact();
    }

    // JWT 토큰 복호화, 정보 반환
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null || claims.get(MEMBER_ID_KEY) == null) {
            throw new TokenException(TokenErrorCode.INVALID_TOKEN);
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Long memberId = Long.valueOf(claims.get(MEMBER_ID_KEY).toString());

        // memberId를 포함한 UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new CustomUserDetails(memberId, claims.getSubject(), authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(accessToken).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}

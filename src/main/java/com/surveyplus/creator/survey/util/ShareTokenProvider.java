package com.surveyplus.creator.survey.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

// 설문 공유 링크(인트로 진입 URL)에 쓰이는 서명 토큰 발급/검증
// 형식: Base64Url("{surveyId}.{N|T}.{HMAC 서명}") - N/T는 일반/테스트 링크 구분이며 서명에 포함되어 위조 불가
// 서버 비밀키 없이는 다른 surveyId용 토큰을 만들거나, 일반 토큰을 테스트 토큰으로 바꿔치기할 수 없음
@Component
public class ShareTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int SIGNATURE_LENGTH = 20; // 서명 길이(문자 수) - 링크를 짧게 유지하기 위해 잘라서 사용
    private static final String TEST_FLAG = "T";
    private static final String NORMAL_FLAG = "N";

    private final SecretKeySpec secretKey;

    public ShareTokenProvider(@Value("${share.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    // 결과를 담는 값 객체 (원본 설문 ID + 테스트 링크 여부)
    public record ParsedToken(Long surveyId, boolean isTest) {
    }

    // 설문 ID로 공유 토큰을 생성 (isTest=true면 설문 상태와 무관하게 응답 흐름을 체험할 수 있는 테스트 링크)
    public String generate(Long surveyId, boolean isTest) {
        String flag = isTest ? TEST_FLAG : NORMAL_FLAG;
        String payload = surveyId + "." + flag + "." + sign(surveyId, isTest);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰을 검증하고, 유효하면 원본 설문 ID/테스트 여부를 반환 (형식이 잘못됐거나 서명이 다르면 empty)
    public Optional<ParsedToken> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        String[] parts = payload.split("\\.", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }

        Long surveyId;
        try {
            surveyId = Long.valueOf(parts[0]);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        boolean isTest;
        if (TEST_FLAG.equals(parts[1])) {
            isTest = true;
        } else if (NORMAL_FLAG.equals(parts[1])) {
            isTest = false;
        } else {
            return Optional.empty();
        }

        String providedSignature = parts[2];
        String expectedSignature = sign(surveyId, isTest);

        boolean matches = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8)
        );

        return matches ? Optional.of(new ParsedToken(surveyId, isTest)) : Optional.empty();
    }

    private String sign(Long surveyId, boolean isTest) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKey);
            String data = surveyId + ":" + (isTest ? TEST_FLAG : NORMAL_FLAG);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.substring(0, Math.min(SIGNATURE_LENGTH, encoded.length()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("공유 토큰 서명 생성에 실패했습니다.", e);
        }
    }
}

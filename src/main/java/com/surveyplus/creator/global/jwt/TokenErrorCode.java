package com.surveyplus.creator.global.jwt;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorCode implements BaseErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TKE001", "로그인 정보가 만료되었습니다. 다시 로그인해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

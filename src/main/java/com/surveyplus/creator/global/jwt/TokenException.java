package com.surveyplus.creator.global.jwt;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serial;

@Getter
@RequiredArgsConstructor
public class TokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final BaseErrorCode errorCode;

    @Override
    public String getMessage() {
        return errorCode.getMessage();
    }
}

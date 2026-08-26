package com.surveyplus.creator.answer.exception;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serial;

@Getter
@RequiredArgsConstructor
public class AnswerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final BaseErrorCode errorCode;

    @Override
    public String getMessage() {
        return errorCode.getMessage();
    }
}

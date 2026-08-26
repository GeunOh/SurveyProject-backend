package com.surveyplus.creator.survey.exception;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class QuestionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final BaseErrorCode errorCode;

    @Override
    public String getMessage() {
        return errorCode.getMessage();
    }
}

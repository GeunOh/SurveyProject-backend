package com.surveyplus.creator.survey.exception;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements BaseErrorCode {
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QE001", "존재하지 않는 질문입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

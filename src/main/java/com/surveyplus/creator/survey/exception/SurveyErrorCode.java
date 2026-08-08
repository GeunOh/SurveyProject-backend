package com.surveyplus.creator.survey.exception;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SurveyErrorCode implements BaseErrorCode {
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "SVE001", "존재하지 않는 설문입니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "SVE002", "해당 설문에 대한 권한이 없습니다."),
    INVALID_SURVEY_STATUS(HttpStatus.BAD_REQUEST, "SVE003", "올바르지 않은 설문 상태 값입니다."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SVE004", "존재하지 않는 질문입니다."),
    CHOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "SVE005", "존재하지 않는 보기입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

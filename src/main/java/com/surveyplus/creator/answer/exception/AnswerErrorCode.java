package com.surveyplus.creator.answer.exception;

import com.surveyplus.creator.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AnswerErrorCode implements BaseErrorCode {
    SESSION_ID_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AE001", "세션 ID 생성에 실패했습니다. 잠시 후 다시 시도해 주세요."),
    ANSWER_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "AE002", "존재하지 않거나 만료된 설문 응답 세션입니다."),
    INVALID_SURVEY_STATUS(HttpStatus.BAD_REQUEST, "AE003", "진행 중인 설문 응답 상태가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

package com.surveyplus.creator.answer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerStatus {
    PROGRESS("응답중"),
    SCREEN("스크리닝 탈락"),
    QUOTAOUT("정원마감으로 중단"),
    END("완료");

    private final String description;
}

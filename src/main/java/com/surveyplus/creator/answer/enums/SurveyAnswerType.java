package com.surveyplus.creator.answer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SurveyAnswerType {
    TEST("테스트형"),
    BANNER("배너형"),
    LIST("목록형");

    private final String description;
}

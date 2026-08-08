package com.surveyplus.creator.survey.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SurveyStatus {
    ALREADY("준비중"),
    ACTIVE("진행중"),
    PAUSED("일시중지"),
    CLOSED("종료");

    private final String description;
}

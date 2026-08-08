package com.surveyplus.creator.survey.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SurveyOptionKey {
    PERIOD("설문 기간"),
    LIMIT("인원 제한"),
    THEME("설문 색상 테마");

    private final String description;
}

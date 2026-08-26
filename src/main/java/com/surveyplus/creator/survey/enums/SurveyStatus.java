package com.surveyplus.creator.survey.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SurveyStatus {
    ALREADY("준비중"),
    ACTIVE("진행중"),
    PAUSED("일시중지"),
    CLOSED("종료"),
    QUOTA_OUT("정원마감"),
    // 실제 DB에는 저장되지 않는 가상 상태 - 응답 기간(PERIOD) 옵션의 종료일이 지났을 때 응답자에게만 표시됨
    PERIOD_END("응답기간 종료");

    private final String description;
}

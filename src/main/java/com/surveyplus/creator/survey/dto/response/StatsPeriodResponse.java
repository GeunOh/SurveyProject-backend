package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 특정 기간 동안의 응답 통계 스냅샷 ("기간 대비 증감" 비교의 한 쪽) */
@Getter
@Builder
public class StatsPeriodResponse {
    private long totalResponses;
    private double avgResponseRate;
    private long avgResponseSeconds;
}

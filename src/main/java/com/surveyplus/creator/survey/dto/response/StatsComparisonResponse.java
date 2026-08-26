package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

/** "지난 기간 대비" 카드용 - 선택된 기간과 그 직전 동일 길이 기간의 통계 비교 */
@Getter
@Builder
public class StatsComparisonResponse {
    private StatsPeriodResponse current;
    private StatsPeriodResponse previous;
}

package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 요일·시간대별 응답 히트맵의 셀 하나 */
@Getter
@Builder
public class HeatmapCellResponse {
    private int day;  // 0=월 ~ 6=일
    private int hour; // 0~23
    private long count;
}

package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 홈 대시보드 응답 추이 차트용 - 하루 단위 완료 응답 수 */
@Getter
@Builder
public class ResponseTrendItem {
    private String date;  // yyyy-MM-dd
    private long count;
}

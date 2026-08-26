package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** 응답 시간 분포 통계 - 평균/중앙값/최대 소요시간 및 구간별 히스토그램 */
@Getter
@Builder
public class ResponseTimeStatsResponse {
    private long averageSeconds;
    private long medianSeconds;
    private long maxSeconds;
    private List<DurationBucketItem> buckets;
}

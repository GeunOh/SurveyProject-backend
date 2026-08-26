package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

/** 응답 소요시간 분포 히스토그램의 구간 하나 */
@Getter
@Builder
public class DurationBucketItem {
    private String label;
    private long count;
}

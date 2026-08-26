package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 홈 대시보드 최근 응답 현황용 - 익명 식별키 + 설문 제목 + 응답 완료 시각 */
@Getter
@Builder
public class RecentResponseItem {
    private String anonymousId;
    private String surveyTitle;
    private LocalDateTime respondedAt;
}

package com.surveyplus.creator.survey.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyStatsResponse {
    // 삭제되지 않은 전체 설문 수
    private long totalSurveys;
    // 진행중(ACTIVE)인 설문 수
    private long activeSurveys;
    // 종료(CLOSED)된 설문 수
    private long closedSurveys;
    // 준비중(ALREADY)인 설문 수
    private long alreadySurveys;
    // 일시중지(PAUSED)된 설문 수
    private long pausedSurveys;
    // 완료(END) 처리된 실제 응답 수 (테스트 응답 제외)
    private long totalResponses;
    // 회원이 소유한 설문들의 응답률 평균 (인원 제한 없는 설문은 0%로 계산에 포함)
    private double avgResponseRate;
}

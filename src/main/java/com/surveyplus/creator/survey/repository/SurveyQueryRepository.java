package com.surveyplus.creator.survey.repository;

import com.surveyplus.creator.survey.dto.request.SurveySearchCondition;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveyQueryRepository {
    Page<SurveyListResponse> getSurveyList(SurveySearchCondition condition, Pageable pageable);

    // 대시보드 통계용 - 회원이 소유한 설문들의 응답률 평균 (인원 제한 없는 설문은 0%로 계산에 포함)
    double getAverageResponseRate(Long memberId);
}

package com.surveyplus.creator.survey.repository;

import com.surveyplus.creator.survey.dto.request.SurveySearchCondition;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveyQueryRepository {
    Page<SurveyListResponse> getSurveyList(SurveySearchCondition condition, Pageable pageable);
}

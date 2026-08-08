package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.enums.SurveyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SurveyStatusUpdateRequest {
    private Long memberId;
    private SurveyStatus surveyStatus;
}

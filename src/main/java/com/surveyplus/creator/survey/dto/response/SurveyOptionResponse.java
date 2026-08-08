package com.surveyplus.creator.survey.dto.response;

import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SurveyOptionResponse {
    private Long id;
    private SurveyOptionKey key;
    private String value;
    private Boolean isActive;
}

package com.surveyplus.creator.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChoiceOptionResponse {
    private Long id;
    private String key;
    private String value;
}

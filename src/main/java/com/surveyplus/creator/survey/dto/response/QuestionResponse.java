package com.surveyplus.creator.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class QuestionResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private Integer order;
    private Boolean required;
    private List<ChoiceResponse> choices;
    private Map<String, String> options;
}

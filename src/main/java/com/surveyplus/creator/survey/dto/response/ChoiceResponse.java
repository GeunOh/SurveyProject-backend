package com.surveyplus.creator.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ChoiceResponse {
    private Long id;
    private String text;
    private Integer order;
    private List<ChoiceOptionResponse> options;
}

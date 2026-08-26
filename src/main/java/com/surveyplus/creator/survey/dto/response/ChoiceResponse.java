package com.surveyplus.creator.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ChoiceResponse {
    private Long id;
    private String text;
    private Integer order;
    // 보기별 부가 옵션 (key/value)
    private Map<String, String> options;
}

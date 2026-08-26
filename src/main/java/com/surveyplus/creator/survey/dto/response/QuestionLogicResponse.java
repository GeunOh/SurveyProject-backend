package com.surveyplus.creator.survey.dto.response;

import com.surveyplus.creator.survey.entity.LogicCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QuestionLogicResponse {
    private Long id;
    private Long sourceQuestionId;
    private String targetType;
    private Long targetQuestionId;
    private LogicCondition condition;
}

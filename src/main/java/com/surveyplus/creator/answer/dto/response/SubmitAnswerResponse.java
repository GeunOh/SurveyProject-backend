package com.surveyplus.creator.answer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubmitAnswerResponse {
    private Long nextQuestionId;
    private Integer nextOrder;
    private boolean isCompleted;
}

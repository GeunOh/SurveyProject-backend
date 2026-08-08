package com.surveyplus.creator.answer.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnswerItemRequest {
    private Long choiceId;
    private String text;
}

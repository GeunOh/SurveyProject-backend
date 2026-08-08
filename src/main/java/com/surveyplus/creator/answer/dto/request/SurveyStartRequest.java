package com.surveyplus.creator.answer.dto.request;

import com.surveyplus.creator.answer.entity.AnswerSession;
import com.surveyplus.creator.answer.enums.SurveyAnswerType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SurveyStartRequest {
    private Long surveyId;
    private String answerId;
    private SurveyAnswerType surveyAnswerType;

    public AnswerSession toEntity() {
        return AnswerSession.builder()
                .surveyId(this.surveyId)
                .answerId(this.answerId)
                .surveyType(this.surveyAnswerType)
                .build();
    }
}

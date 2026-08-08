package com.surveyplus.creator.answer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerResponse {
    private Long id;
    private String answerId;
    private Long questionId;
    private Long choiceId;
    private String answerText;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deletedAt;
}

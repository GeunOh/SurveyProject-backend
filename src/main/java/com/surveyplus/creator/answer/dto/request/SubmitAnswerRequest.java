package com.surveyplus.creator.answer.dto.request;

import com.surveyplus.creator.answer.entity.ResponseAnswer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
public class SubmitAnswerRequest {
    private Long surveyId;
    private String answerId;
    private Long questionId;
    private String startedAt;
    private String completedAt;
    private List<AnswerItemRequest> answer;

    public List<ResponseAnswer> toEntities() {
        LocalDateTime parsedStartedAt = LocalDateTime.parse(this.startedAt, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime parsedCompletedAt = LocalDateTime.parse(this.completedAt, DateTimeFormatter.ISO_DATE_TIME);

        return this.answer.stream()
            .map(item -> ResponseAnswer.builder()
                .answerId(this.answerId)
                .questionId(this.questionId)
                .choiceId(item.getChoiceId())
                .answerText(item.getText())
                .startedAt(parsedStartedAt)
                .completedAt(parsedCompletedAt)
                .build())
            .toList();
    }
}

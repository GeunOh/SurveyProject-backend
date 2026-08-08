package com.surveyplus.creator.answer.dto.response;

import com.surveyplus.creator.answer.util.SurveyTimeCalculator;
import com.surveyplus.creator.survey.dto.response.QuestionResponse;
import com.surveyplus.creator.survey.dto.response.SurveyOptionResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SurveyIntroResponse {
    private String answerId;
    private String title;
    private String description;
    private int estimatedTime;
    private int totalQuestions;
    private List<QuestionResponse> questions;
    private List<SurveyOptionResponse> options;

    public static SurveyIntroResponse of(
            String answerId,
            String title,
            String description,
            List<QuestionResponse> questions,
            List<SurveyOptionResponse> options
    ) {
        int totalQuestions = questions != null ? questions.size() : 0;

        return SurveyIntroResponse.builder()
                .answerId(answerId)
                .title(title)
                .description(description)
                .estimatedTime(calculateTotalSeconds(questions))
                .totalQuestions(totalQuestions)
                .questions(questions)
                .options(options)
                .build();
    }

    // 문항 수 기반 예상 소요 시간 계산 메서드
    private static int calculateTotalSeconds(List<QuestionResponse> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }

        return questions.stream()
                .mapToInt(q -> SurveyTimeCalculator.getQuestionSeconds(q.getType()))
                .sum();
    }
}

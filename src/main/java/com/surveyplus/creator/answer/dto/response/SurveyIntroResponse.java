package com.surveyplus.creator.answer.dto.response;

import com.surveyplus.creator.answer.util.SurveyTimeCalculator;
import com.surveyplus.creator.survey.dto.response.QuestionResponse;
import com.surveyplus.creator.survey.dto.response.SurveyOptionResponse;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SurveyIntroResponse {
    // 공유 토큰 검증을 통과한 이후에만 알려주는 실제 설문 ID (이후 시작/문항조회/제출 호출에 사용)
    private Long surveyId;
    private String answerId;
    private String title;
    private String description;
    // 설문 진행 상태 (ACTIVE가 아니면 프론트에서 응답 종료 안내 페이지로 분기 - 단, 테스트 링크는 예외)
    private SurveyStatus surveyStatus;
    // 테스트 링크로 진입했는지 여부 (프론트가 시작/제출 시 surveyAnswerType=TEST로 표시하도록 안내)
    private boolean isTest;
    private int estimatedTime;
    private int totalQuestions;
    private List<QuestionResponse> questions;
    private List<SurveyOptionResponse> options;

    public static SurveyIntroResponse of(
            Long surveyId,
            String answerId,
            String title,
            String description,
            SurveyStatus surveyStatus,
            boolean isTest,
            List<QuestionResponse> questions,
            List<SurveyOptionResponse> options
    ) {
        int totalQuestions = questions != null ? questions.size() : 0;

        return SurveyIntroResponse.builder()
                .surveyId(surveyId)
                .answerId(answerId)
                .title(title)
                .description(description)
                .surveyStatus(surveyStatus)
                .isTest(isTest)
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

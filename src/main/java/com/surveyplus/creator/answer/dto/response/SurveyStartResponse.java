package com.surveyplus.creator.answer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyStartResponse {
    private Long surveyId;
    private String answerId;
    private Long prevQuestionId;
    private Long questionId;

//    /**
//     * 엔티티로부터 응답 DTO를 생성하는 팩토리 메서드
//     */
//    public static SurveyStartResponse from(SurveyResponse surveyResponse) {
//        return SurveyStartResponse.builder()
//                .surveyId(surveyResponse.getSurveyId())
//                .answerId(surveyResponse.getAnswerId())
//                .questionId(surveyResponse.getQuestionId())
//                .prevQuestionId(surveyResponse.getPrevQuestionId()) // 필드가 있다면 매핑
//                .surveyAnswerType(surveyResponse.getSurveyAnswerType())
//                .responseStatus(surveyResponse.getResponseStatus())
//                .build();
//    }
}

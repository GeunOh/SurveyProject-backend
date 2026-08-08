package com.surveyplus.creator.answer.dto.response;

import com.surveyplus.creator.survey.dto.response.ChoiceResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionDetailResponse {

    private Long questionId;              // 질문 ID
    private String title;                 // 질문 내용 (예: "만족도를 선택해주세요")
    private String description;           // 질문 부가 설명 (필요한 경우)
    private String type;                  // 질문 유형
    private boolean isRequired;           // 필수 응답 여부
    private int order;                    // 문항 순서
    private int estimatedTime;            // 남은 예상 소요 시간 (초)
    private List<ChoiceResponse> choices; // 보기 목록
    private List<SurveyAnswerResponse> answers; // 응답 목록

}

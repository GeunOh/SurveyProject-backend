package com.surveyplus.creator.answer.dto.response;

import com.surveyplus.creator.survey.dto.response.ChoiceResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

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
    private Map<String, String> options;  // 질문별 부가 옵션 (글자수 제한, 보기 랜덤 정렬 등)
    private int totalQuestions;           // 설문 전체 문항 수 (응답 도중 설문이 수정될 수 있어 매 조회마다 최신값으로 내려줌)
    private String themeColor;            // 설문 테마 색상 (응답 도중 설문이 수정될 수 있어 매 조회마다 최신값으로 내려줌)

}

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
    // 완료(isCompleted=true)이지만 정상 종료가 아니라 분기 로직에 의한 중도 탈락(스크리닝 아웃)인 경우 true
    private boolean screened;
}

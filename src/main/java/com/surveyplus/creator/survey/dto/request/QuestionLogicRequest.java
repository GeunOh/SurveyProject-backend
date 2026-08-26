package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.entity.LogicCondition;
import com.surveyplus.creator.survey.entity.QuestionLogic;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class QuestionLogicRequest {

    // 기준 질문 (이미 저장되어 실제 id가 있는 질문만 허용 - 신규 미저장 질문은 프론트에서 후보로 노출하지 않음)
    @NotNull(message = "기준 질문은 필수입니다.")
    private Long sourceQuestionId;

    @Pattern(regexp = "QUESTION|END|SCREEN", message = "이동 대상 유형은 QUESTION, END, SCREEN 중 하나여야 합니다.")
    private String targetType;

    // targetType이 QUESTION일 때만 필요
    private Long targetQuestionId;

    @NotNull(message = "답변 조건은 필수입니다.")
    @Valid
    private LogicCondition condition;

    public QuestionLogic toEntity() {
        return QuestionLogic.builder()
                .sourceQuestionId(this.sourceQuestionId)
                .targetType(this.targetType)
                .targetQuestionId("QUESTION".equals(this.targetType) ? this.targetQuestionId : null)
                .conditionJson(this.condition)
                .build();
    }
}

package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.survey.dto.response.QuestionLogicResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "question_logic", indexes = {
        @Index(name = "idx_source_question_id", columnList = "source_question_id")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuestionLogic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기준 질문 (이 질문에 대한 답변에 따라 분기됨) - DB에는 FK 없이 순수 값으로만 저장 (프로젝트 전반의 컨벤션)
    @Column(name = "source_question_id", nullable = false)
    private Long sourceQuestionId;

    // 이동 대상 종류 - "QUESTION" | "END"
    @Column(name = "target_type", nullable = false)
    private String targetType;

    // targetType이 QUESTION일 때만 값이 존재
    @Column(name = "target_question_id")
    private Long targetQuestionId;

    // 답변 조건 (그룹형 AND/OR 구조) - JSON 컬럼에 그대로 저장
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_json", columnDefinition = "json", nullable = false)
    private LogicCondition conditionJson;

    public QuestionLogicResponse from() {
        return QuestionLogicResponse.builder()
                .id(this.id)
                .sourceQuestionId(this.sourceQuestionId)
                .targetType(this.targetType)
                .targetQuestionId(this.targetQuestionId)
                .condition(this.conditionJson)
                .build();
    }
}

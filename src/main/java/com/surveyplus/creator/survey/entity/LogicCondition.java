package com.surveyplus.creator.survey.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

// 분기 로직의 답변 조건 전체 (question_logic.condition_json 컬럼에 그대로 저장되는 JSON 구조)
// 예: (1번 OR 2번) AND (3번 OR 4번) -> { operator: "AND", groups: [{operator:"OR", items:[{choiceId:1},{choiceId:2}]}, ...] }
// JSON 컬럼 구조가 바뀌어도 옛 데이터를 읽다가 서버가 죽지 않도록 알 수 없는 필드는 무시
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogicCondition {

    @Pattern(regexp = "AND|OR", message = "조건 결합 방식은 AND 또는 OR만 가능합니다.")
    private String operator;

    @NotEmpty(message = "최소 1개의 조건 그룹이 필요합니다.")
    @Valid
    private List<LogicConditionGroup> groups;
}

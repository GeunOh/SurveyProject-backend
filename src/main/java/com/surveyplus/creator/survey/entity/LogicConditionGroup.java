package com.surveyplus.creator.survey.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;

// 로직 조건의 그룹 하나 (그룹 내 조건 항목들을 AND/OR로 결합) - question_logic.condition_json 안에 중첩 저장됨
// JSON 컬럼 구조가 바뀌어도(예: 예전 choiceIds -> 지금 items) 옛 데이터를 읽다가 서버가 죽지 않도록 알 수 없는 필드는 무시
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogicConditionGroup {

    @Pattern(regexp = "AND|OR", message = "그룹 결합 방식은 AND 또는 OR만 가능합니다.")
    private String operator;

    @NotEmpty(message = "조건 그룹에는 최소 1개의 조건이 필요합니다.")
    @Valid
    private List<LogicConditionItem> items;
}

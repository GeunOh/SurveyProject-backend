package com.surveyplus.creator.survey.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Pattern;
import lombok.*;

// 조건 그룹 안의 조건 항목 하나 - question_logic.condition_json 안에 중첩 저장됨
// 두 가지 형태로 쓰임:
//  1) 객관식(A03/A04) 보기 선택 조건: choiceId만 사용 (compare/value는 null)
//  2) 값 비교 조건(A01/A02/A05/A06): compare + value 사용. choiceId는 A01처럼 여러 입력 필드가 있는 경우에만
//     "어느 필드를 비교할지"를 가리키는 용도로 함께 쓰이고, 필드가 하나뿐인 A02/A05/A06는 null
// JSON 컬럼 구조가 바뀌어도 옛 데이터를 읽다가 서버가 죽지 않도록 알 수 없는 필드는 무시
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogicConditionItem {

    // 객관식 보기 또는 단답형(A01) 입력 필드의 식별자
    private Long choiceId;

    // 값 비교 연산자 - "EQ"(일치/정확히) | "GTE"(이상) | "GT"(초과) | "LTE"(이하) | "LT"(미만) | "CONTAINS"(포함)
    // null이면 choiceId 보기를 선택했는지 여부 자체가 조건
    @Pattern(regexp = "EQ|GTE|GT|LTE|LT|CONTAINS", message = "비교 연산자는 EQ, GTE, GT, LTE, LT, CONTAINS 중 하나여야 합니다.")
    private String compare;

    // compare가 있을 때 비교 대상 값
    private String value;
}

package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.survey.dto.response.ChoiceResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "question_choice", indexes = {
        @Index(name = "idx_question_id", columnList = "question_id")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "choice_text", nullable = false)
    private String text;

    @Builder.Default
    @Column(name = "choice_order", nullable = false)
    private Integer order = 0;

    // 보기별 부가 옵션 (입력 형식, placeholder, 최소/최대 값 등) - key/value 테이블 대신 JSON 컬럼 하나로 관리
    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "json")
    private Map<String, String> options = new HashMap<>();

    public void assignQuestion(Question question) {
        this.question = question;
    }

    public ChoiceResponse from() {
        return ChoiceResponse.builder()
                .id(this.id)
                .text(this.text)
                .order(this.order)
                .options(this.options)
                .build();
    }

    public void update(String text, Integer order) {
        this.text = text;
        this.order = order;
    }

    // JSON 컬럼은 부분 수정 개념이 없는 통째 교체 방식 - 요청으로 온 값을 그대로 반영 (null이면 컬럼도 null)
    public void updateOptions(Map<String, String> options) {
        this.options = options;
    }
}
package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.answer.dto.response.SurveyAnswerResponse;
import com.surveyplus.creator.answer.dto.response.QuestionDetailResponse;
import com.surveyplus.creator.survey.dto.response.ChoiceResponse;
import com.surveyplus.creator.survey.dto.response.QuestionResponse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "question")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "question_type", nullable = false)
    private String type;

    @Column(name = "question_order", nullable = false)
    private Integer order;

    @Column(name = "is_required", nullable = false)
    private Boolean required;

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Choice> choices = new ArrayList<>();

    // 질문별 부가 옵션 (글자수 제한, 보기 랜덤 정렬 등) - Choice.options와 동일하게 key/value를 JSON 컬럼 하나로 관리
    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "json")
    private Map<String, String> options = new HashMap<>();

    public void assignSurvey(Survey survey) {
        this.survey = survey;
    }

    public QuestionResponse from() {
        List<ChoiceResponse> choiceResponses = this.choices.stream()
                .map(Choice::from)
                .collect(Collectors.toList());

        return QuestionResponse.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .type(this.type)
                .order(this.order)
                .required(this.required)
                .choices(choiceResponses)
                .options(this.options)
                .build();
    }

    public QuestionDetailResponse fromDetail(int estimatedTime, List<SurveyAnswerResponse> savedAnswers, int totalQuestions, String themeColor) {
        List<ChoiceResponse> choiceResponses = this.choices.stream()
                .map(Choice::from)
                .collect(Collectors.toList());

        return QuestionDetailResponse.builder()
                .questionId(this.id)
                .title(this.title)
                .description(this.description)
                .type(this.type)
                .order(this.order)
                .estimatedTime(estimatedTime)
                .isRequired(this.required)
                .totalQuestions(totalQuestions)
                .themeColor(themeColor)
                .choices(choiceResponses)
                .answers(savedAnswers)
                .options(this.options)
                .build();
    }

    public void update(String title, String description, String type, Integer order, Boolean required) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.order = order;
        this.required = required;
    }

    // JSON 컬럼은 부분 수정 개념이 없는 통째 교체 방식 - 요청으로 온 값을 그대로 반영 (null이면 컬럼도 null)
    public void updateOptions(Map<String, String> options) {
        this.options = options;
    }
}

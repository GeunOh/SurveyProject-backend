package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.answer.dto.response.AnswerResponse;
import com.surveyplus.creator.answer.dto.response.QuestionDetailResponse;
import com.surveyplus.creator.survey.dto.response.ChoiceResponse;
import com.surveyplus.creator.survey.dto.response.QuestionResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
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
                .build();
    }

    public QuestionDetailResponse fromDetail(int estimatedTime, List<AnswerResponse> savedAnswers) {
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
                .choices(choiceResponses)
                .answers(savedAnswers)
                .build();
    }

    public void update(String title, String description, String type, Integer order, Boolean required) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.order = order;
        this.required = required;
    }
}

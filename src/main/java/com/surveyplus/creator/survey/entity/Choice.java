package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.survey.dto.response.ChoiceOptionResponse;
import com.surveyplus.creator.survey.dto.response.ChoiceResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    @OneToMany(mappedBy = "choice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoiceOption> options = new ArrayList<>();

    public void assignQuestion(Question question) {
        this.question = question;
    }

    public ChoiceResponse from() {
        List<ChoiceOptionResponse> choiceOptions = this.options.stream()
                .map(ChoiceOption::from)
                .toList();

        return ChoiceResponse.builder()
                .id(this.id)
                .text(this.text)
                .order(this.order)
                .options(choiceOptions)
                .build();
    }

    public void update(String text, Integer order) {
        this.text = text;
        this.order = order;
    }
}
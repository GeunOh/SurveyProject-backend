package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.entity.Choice;
import com.surveyplus.creator.survey.entity.Question;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionRequest {

    private Long id;

    @NotBlank(message = "질문 제목은 필수입니다.")
    private String title;

    private String description;

    @NotBlank(message = "질문 타입은 필수입니다.")
    private String type;

    @NotNull(message = "질문 순서는 필수입니다.")
    private Integer order;

    @NotNull(message = "필수 응답 여부는 필수입니다.")
    private Boolean required;

    @Valid
    private List<QuestionChoiceRequest> choices;

    public Question toEntity() {
        Question question = Question.builder()
                .title(this.title)
                .description(this.description)
                .type(this.type)
                .order(this.order)
                .required(this.required != null ? this.required : false)
                .build();

        if (this.choices != null) {
            List<Choice> choices = this.choices.stream()
                    .map(QuestionChoiceRequest::toEntity)
                    .peek(choice -> choice.assignQuestion(question))
                    .toList();
            question.getChoices().addAll(choices);
        }

        return question;
    }
}

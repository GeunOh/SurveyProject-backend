package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.entity.Choice;
import com.surveyplus.creator.survey.entity.ChoiceOption;
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
public class ChoiceRequest {

    private Long id;

    @NotBlank(message = "보기 텍스트는 필수입니다.")
    private String text;

    @NotNull(message = "보기 순서는 필수입니다.")
    private Integer order;

    @Valid
    private List<ChoiceOptionRequest> options;

    public Choice toEntity() {
        Choice choice = Choice.builder()
                .text(this.text)
                .order(this.order != null ? this.order : 0)
                .build();

        if (this.options != null) {
            List<ChoiceOption> choiceOptions = this.options.stream()
                    .map(ChoiceOptionRequest::toEntity)
                    .peek(attr -> attr.assignChoice(choice))
                    .toList();
            choice.getOptions().addAll(choiceOptions);
        }

        return choice;
    }
}

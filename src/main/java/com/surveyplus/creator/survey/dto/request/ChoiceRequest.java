package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.entity.Choice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

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

    // 보기별 부가 옵션 (key/value) - JSON 컬럼에 그대로 저장됨
    private Map<String, String> options;

    public Choice toEntity() {
        Choice choice = Choice.builder()
                .text(this.text)
                .order(this.order != null ? this.order : 0)
                .build();

        choice.updateOptions(this.options);

        return choice;
    }
}

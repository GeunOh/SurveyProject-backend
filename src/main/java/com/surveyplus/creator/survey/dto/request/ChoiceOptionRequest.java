package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.entity.ChoiceOption;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceOptionRequest {

    private Long id;

    @NotBlank(message = "옵션 키는 필수입니다.")
    private String key;

    private String value;

    public ChoiceOption toEntity() {
        return ChoiceOption.builder()
                .key(this.key)
                .value(this.value)
                .build();
    }
}

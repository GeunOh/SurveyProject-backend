package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.entity.SurveyOption;
import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SurveyOptionRequest {

    private Long id;

    @NotNull(message = "옵션 키(key)는 필수입니다.")
    private SurveyOptionKey key;

    private String value;

    private Boolean isActive;

    public SurveyOption toEntity() {
        return SurveyOption.builder()
                .key(this.key)
                .value(this.value)
                .isActive(this.isActive != null ? this.isActive : true)
                .build();
    }
}
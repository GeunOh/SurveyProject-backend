package com.surveyplus.creator.answer.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionDetailRequest {

    @NotNull(message = "설문 ID는 필수입니다.")
    private Long surveyId;

    @NotNull(message = "질문 ID는 필수입니다.")
    private Long questionId;

    @NotNull(message = "응답 ID는 필수입니다.")
    private String answerId;
}

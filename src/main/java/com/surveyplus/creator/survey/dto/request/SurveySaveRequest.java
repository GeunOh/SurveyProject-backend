package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.entity.Survey;
import com.surveyplus.creator.survey.entity.SurveyOption;
import com.surveyplus.creator.survey.entity.Question;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SurveySaveRequest {

    private Long id;

    @NotBlank(message = "설문 제목은 필수 입력 항목입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    private String title;

    @Size(max = 500, message = "설명은 500자 이내로 입력해주세요.")
    private String description;

    @NotNull(message = "설문 상태는 필수입니다.")
    private SurveyStatus surveyStatus;

    @Valid
    private List<SurveyOptionRequest> options;

    @Valid
    private List<SurveyQuestionRequest> questions;

    // 응답 분기 로직 목록 - 신규 생성 시점에는 항상 비어있음(기준/대상 질문이 아직 저장 전이라 실제 id가 없음), 수정 시에만 사용
    @Valid
    private List<QuestionLogicRequest> logics;


    public Survey toEntity(Long memberId) {
        Survey survey = Survey.builder()
                .memberId(memberId)
                .title(this.title)
                .description(this.description)
                .surveyStatus(this.surveyStatus)
                .build();

        if (this.questions != null) {
            List<Question> questionEntities = this.questions.stream()
                    .map(SurveyQuestionRequest::toEntity)
                    .peek(question -> question.assignSurvey(survey))
                    .toList();

            survey.getQuestions().addAll(questionEntities);
        }

        if (this.options != null) {
            List<SurveyOption> optionEntities = this.options.stream()
                    .map(SurveyOptionRequest::toEntity)
                    .peek(option -> option.assignSurvey(survey))
                    .toList();

            survey.getOptions().addAll(optionEntities);
        }

        return survey;
    }
}

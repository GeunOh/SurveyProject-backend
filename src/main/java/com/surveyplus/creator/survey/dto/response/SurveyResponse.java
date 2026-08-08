package com.surveyplus.creator.survey.dto.response;

import com.surveyplus.creator.survey.enums.SurveyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SurveyResponse {
    private Long id;
    private Long memberId;
    private String title;
    private String description;
    private SurveyStatus surveyStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionResponse> questions;
    private List<SurveyOptionResponse> options;
}

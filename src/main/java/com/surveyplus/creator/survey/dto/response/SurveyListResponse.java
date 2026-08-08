package com.surveyplus.creator.survey.dto.response;

import com.surveyplus.creator.survey.enums.SurveyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SurveyListResponse {
    private Long id;
    private String title;
    private String description;
    private SurveyStatus surveyStatus;
    private int responseCount;              // 참여한 사람 수
    private int targetCount;                // 전체 대상자 수
    private double responseRate;            // 응답률 (%)
    private int peopleLimit;                // 인원제한
    private String surveyDateLimit;         // 응답날짜제한


    public static SurveyListResponse toSurveyCard(SurveyResponse surveyResponse, int responseCount, int targetCount, double responseRate) {

        int peopleLimit = 0;
        String surveyDateLimit = null;
        String surveyColor = null;

        List<SurveyOptionResponse> options = surveyResponse.getOptions();
        if (options != null) {
            for (SurveyOptionResponse option : options) {
                if (option.getKey() == null || option.getValue() == null) {
                    continue;
                }

                // switch 문으로 키값에 따라 분기 처리
                switch (option.getKey()) {
                    case PERIOD:
                        surveyDateLimit = option.getValue();
                        break;
                    case LIMIT:
                        peopleLimit = Integer.parseInt(option.getValue());
                        break;
                    case THEME:
                        surveyColor = option.getValue();
                        break;
                }
            }
        }
        return SurveyListResponse.builder()
                .id(surveyResponse.getId())
                .title(surveyResponse.getTitle())
                .description(surveyResponse.getDescription())
                .surveyStatus(surveyResponse.getSurveyStatus())
                .responseCount(responseCount)
                .targetCount(targetCount)
                .responseRate(responseRate)
                .peopleLimit(peopleLimit)
                .surveyDateLimit(surveyDateLimit)
                .build();
    }
}

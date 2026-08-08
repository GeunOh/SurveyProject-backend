package com.surveyplus.creator.survey.dto.request;

import com.surveyplus.creator.survey.enums.SurveyStatus;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveySearchCondition {
    private Long memberId;
    private String keyword;
    private SurveyStatus status;
//    @Builder.Default
//    private String filter = "최신순";

    public static SurveySearchCondition of(Long memberId, String keyword, SurveyStatus status) {
        return SurveySearchCondition.builder()
                .memberId(memberId)
                .keyword(keyword)
                .status(status)
                .build();
    }
}

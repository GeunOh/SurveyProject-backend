package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.survey.dto.response.SurveyOptionResponse;
import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_options", indexes = {
        @Index(name = "idx_survey_key", columnList = "survey_id, settings_key")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SurveyOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_key", nullable = false, length = 100)
    private SurveyOptionKey key;

    @Column(name = "option_value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = false;


    public void assignSurvey(Survey survey) {
        this.survey = survey;
    }

    public SurveyOptionResponse from() {
        return SurveyOptionResponse.builder()
                .id(this.id)
                .key(this.key)
                .value(this.value)
                .isActive(this.isActive)
                .build();
    }
    
    //설문 옵션 업데이트
    public void updateOption(String value, Boolean isActive) {
        this.value = value;
        // isActive가 null로 넘어올 경우 기본값 true 적용
        this.isActive = isActive != null ? isActive : true;
    }
}

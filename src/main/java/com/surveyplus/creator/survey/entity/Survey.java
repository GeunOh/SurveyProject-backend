package com.surveyplus.creator.survey.entity;

import com.surveyplus.creator.answer.dto.response.SurveyIntroResponse;
import com.surveyplus.creator.survey.dto.response.SurveyOptionResponse;
import com.surveyplus.creator.survey.dto.response.QuestionResponse;
import com.surveyplus.creator.survey.dto.response.SurveyResponse;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import com.surveyplus.creator.survey.exception.SurveyErrorCode;
import com.surveyplus.creator.survey.exception.SurveyException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey")
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "survey_status", nullable = false)
    private SurveyStatus surveyStatus;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "survey_id")
    private List<Question> questions = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "survey_id")
    private List<SurveyOption> options = new ArrayList<>();

    public SurveyResponse from() {
        List<QuestionResponse> questionResponses = this.questions.stream()
                .map(Question::from)
                .toList();

        List<SurveyOptionResponse> optionResponses = this.options.stream()
                .map(SurveyOption::from)
                .toList();

        return SurveyResponse.builder()
                .id(this.id)
                .memberId(this.memberId)
                .title(this.title)
                .description(this.description)
                .surveyStatus(this.surveyStatus)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .questions(questionResponses)
                .options(optionResponses)
                .build();
    }

    public SurveyIntroResponse fromIntro(String answerId) {
        List<QuestionResponse> questionResponses = this.questions.stream()
                .map(Question::from)
                .toList();

        List<SurveyOptionResponse> optionResponses = this.options.stream()
                .map(SurveyOption::from)
                .toList();

        return SurveyIntroResponse.of(
                answerId,
                this.title,
                this.description,
                questionResponses,
                optionResponses
        );
    }
    
    // 설문 상태 업데이트
    public void changeStatus(SurveyStatus newStatus) {
        if (newStatus == null) {
            throw new SurveyException(SurveyErrorCode.INVALID_SURVEY_STATUS);
        }
        this.surveyStatus = newStatus;
    }
    
    // 설문 삭제
    public void deleteSurvey() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    
    //설문 기본 상태 업데이트
    public void updateInfo(String title, String description) {
        this.title = title;
        this.description = description;
    }
}

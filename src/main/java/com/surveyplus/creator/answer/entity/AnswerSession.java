package com.surveyplus.creator.answer.entity;

import com.surveyplus.creator.answer.dto.response.SurveyStartResponse;
import com.surveyplus.creator.answer.enums.AnswerStatus;
import com.surveyplus.creator.answer.enums.SurveyAnswerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "survey_response_status",
    indexes = {
        @Index(name = "idx_survey_response_answer_id", columnList = "answer_id"),
        @Index(name = "idx_survey_response_survey_id", columnList = "survey_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AnswerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Column(name = "answer_id", nullable = false, length = 500, unique = true)
    private String answerId;

    @Column(name = "prev_question_id")
    private Long prevQuestionId;

    @Column(name = "question_id")
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "survey_type", nullable = false, length = 50)
    private SurveyAnswerType surveyType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "response_status", length = 20)
    private AnswerStatus status = AnswerStatus.PROGRESS;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public void updateQuestionProgress(Long previousQuestionId, Long newQuestionId) {
        this.prevQuestionId = previousQuestionId;
        this.questionId = newQuestionId;
    }

    // 설문 시작 리스폰스
    public SurveyStartResponse from() {
        return SurveyStartResponse.builder()
                .surveyId(this.getSurveyId())
                .answerId(this.getAnswerId())
                .questionId(this.getQuestionId())
                .prevQuestionId(this.getPrevQuestionId())
                .build();
    }
    // 설문 최종 제출 시 상태 변경 메서드
    public void complete() {
        this.status = AnswerStatus.END;
        this.endedAt = LocalDateTime.now();
    }

    // 분기 로직에 의해 응답 도중 스크리닝 탈락 처리된 세션을 표시
    public void screen() {
        this.status = AnswerStatus.SCREEN;
        this.endedAt = LocalDateTime.now();
    }

    // 응답 도중 설문이 정원마감되어 더 이상 진행할 수 없게 된 세션을 표시
    public void markQuotaOut() {
        this.status = AnswerStatus.QUOTAOUT;
        this.endedAt = LocalDateTime.now();
    }
}
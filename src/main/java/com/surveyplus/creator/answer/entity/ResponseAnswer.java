package com.surveyplus.creator.answer.entity;

import com.surveyplus.creator.answer.dto.response.AnswerResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "survey_response_answer",
    indexes = {
        @Index(name = "idx_response_answer_response_id", columnList = "response_id"),
        @Index(name = "idx_choice_id", columnList = "choice_id")
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_id", nullable = false)
    private String answerId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "choice_id")
    private Long choiceId;

    @Column(name = "answer_text")
    private String answerText;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    // 답변 수정(Upsert) 시 내용을 갱신하기 위한 메서드
    public void updateAnswer(Long choiceId, String answerText) {
        this.choiceId = choiceId;
        this.answerText = answerText;
    }

    public AnswerResponse from() {
        return AnswerResponse.builder()
                .id(this.id)
                .answerId(this.answerId)
                .questionId(this.questionId)
                .choiceId(this.choiceId)
                .answerText(this.answerText)
                .startedAt(this.startedAt)
                .completedAt(this.completedAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}

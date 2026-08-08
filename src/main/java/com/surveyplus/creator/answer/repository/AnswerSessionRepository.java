package com.surveyplus.creator.answer.repository;

import com.surveyplus.creator.answer.entity.AnswerSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerSessionRepository extends JpaRepository<AnswerSession, Long> {
    boolean existsBySurveyIdAndAnswerId(Long surveyId, String answerId);
    Optional<AnswerSession> findBySurveyIdAndAnswerId(Long surveyId, String answerId);
}

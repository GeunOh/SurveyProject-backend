package com.surveyplus.creator.answer.repository;

import com.surveyplus.creator.answer.entity.ResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyResponseRepository extends JpaRepository<ResponseStatus, Long> {
    boolean existsBySurveyIdAndAnswerId(Long surveyId, String answerId);
    Optional<ResponseStatus> findBySurveyIdAndAnswerId(Long surveyId, String answerId);
}

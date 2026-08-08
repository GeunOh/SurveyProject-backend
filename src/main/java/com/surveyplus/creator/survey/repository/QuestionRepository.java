package com.surveyplus.creator.survey.repository;

import com.surveyplus.creator.survey.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByIdAndSurveyId(Long id, Long surveyId);
}

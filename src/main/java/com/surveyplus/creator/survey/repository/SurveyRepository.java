package com.surveyplus.creator.survey.repository;

import com.surveyplus.creator.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long>, SurveyQueryRepository {
    Optional<Survey> findByIdAndIsDeletedFalse(Long surveyId);

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.id = :id")
    Optional<Survey> findByIdWithQuestions(@Param("id") Long id);
}

package com.surveyplus.creator.answer.repository;

import com.surveyplus.creator.answer.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE SurveyAnswer a SET a.deletedAt = :now WHERE a.answerId = :answerId AND a.questionId = :questionId AND a.deletedAt IS NULL")
    void softDeleteExistingAnswers(@Param("answerId") String answerId,
                                   @Param("questionId") Long questionId,
                                   @Param("now") LocalDateTime now);

    List<SurveyAnswer> findByQuestionIdAndAnswerIdAndDeletedAtIsNull(Long questionId, String answerId);
}

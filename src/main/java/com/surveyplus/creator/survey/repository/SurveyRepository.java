package com.surveyplus.creator.survey.repository;

import com.surveyplus.creator.survey.entity.Survey;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long>, SurveyQueryRepository {
    Optional<Survey> findByIdAndIsDeletedFalse(Long surveyId);

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.id = :id")
    Optional<Survey> findByIdWithQuestions(@Param("id") Long id);

    // 대시보드 통계용 - 회원이 소유한(삭제되지 않은) 전체 설문 수
    long countByMemberIdAndIsDeletedFalse(Long memberId);

    // 대시보드 통계용 - 회원이 소유한(삭제되지 않은) 설문 중 특정 상태의 설문 수
    long countByMemberIdAndIsDeletedFalseAndSurveyStatus(Long memberId, SurveyStatus surveyStatus);

    // 응답 추이/최근 응답 현황 집계용 - 회원이 소유한(삭제되지 않은) 설문 ID 목록
    @Query("SELECT s.id FROM Survey s WHERE s.memberId = :memberId AND s.isDeleted = false")
    List<Long> findIdsByMemberIdAndIsDeletedFalse(@Param("memberId") Long memberId);
}

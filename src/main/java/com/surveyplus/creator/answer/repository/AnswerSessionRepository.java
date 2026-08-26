package com.surveyplus.creator.answer.repository;

import com.surveyplus.creator.answer.entity.AnswerSession;
import com.surveyplus.creator.answer.enums.AnswerStatus;
import com.surveyplus.creator.answer.enums.SurveyAnswerType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AnswerSessionRepository extends JpaRepository<AnswerSession, Long> {
    boolean existsBySurveyIdAndAnswerId(Long surveyId, String answerId);
    Optional<AnswerSession> findBySurveyIdAndAnswerId(Long surveyId, String answerId);
    // 정원(쿼터) 계산용 - 테스트 응답(surveyType=TEST)은 실제 응답 수 집계에서 제외
    long countBySurveyIdAndStatusAndSurveyTypeNot(Long surveyId, AnswerStatus status, SurveyAnswerType surveyType);

    // 대시보드 통계용 - 회원이 소유한 모든 설문에 걸친 완료 응답 수 (테스트 응답 제외)
    @Query("SELECT COUNT(a) FROM AnswerSession a " +
            "WHERE a.status = :status AND a.surveyType <> :excludeType " +
            "AND a.surveyId IN (SELECT s.id FROM Survey s WHERE s.memberId = :memberId AND s.isDeleted = false)")
    long countCompletedResponsesByMemberId(@Param("memberId") Long memberId,
                                            @Param("status") AnswerStatus status,
                                            @Param("excludeType") SurveyAnswerType excludeType);

    // 홈 대시보드/통계 분석 응답 추이·히트맵·응답시간분포용 - 지정된 설문들에서, 특정 시점 이후 완료된 응답들
    // (surveyTypes로 테스트 응답 포함 여부를 호출 측에서 결정 - 일자별 집계 등은 서비스 레이어에서 처리)
    List<AnswerSession> findBySurveyIdInAndStatusAndSurveyTypeInAndEndedAtGreaterThanEqual(
            List<Long> surveyIds, AnswerStatus status, List<SurveyAnswerType> surveyTypes, LocalDateTime from);

    // 홈 대시보드 최근 응답 현황용 - 지정된 설문들에서, 가장 최근에 완료된 응답 N건
    List<AnswerSession> findBySurveyIdInAndStatusAndSurveyTypeNotOrderByEndedAtDesc(
            List<Long> surveyIds, AnswerStatus status, SurveyAnswerType surveyType, Pageable pageable);

    // 통계 분석 페이지의 "기간 대비 증감" 비교용 - 지정된 설문들에서, 특정 구간(start~end) 사이에 완료된 응답들
    List<AnswerSession> findBySurveyIdInAndStatusAndSurveyTypeInAndEndedAtBetween(
            List<Long> surveyIds, AnswerStatus status, List<SurveyAnswerType> surveyTypes, LocalDateTime from, LocalDateTime to);
}

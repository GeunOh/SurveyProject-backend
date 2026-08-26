package com.surveyplus.creator.survey.repository;

import com.surveyplus.creator.survey.entity.SurveyOption;
import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {
    Optional<SurveyOption> findBySurveyIdAndKeyAndIsActiveTrue(Long surveyId, SurveyOptionKey key);

    // 통계 분석의 기간별 평균 응답률 계산용 - 지정된 설문들의 활성화된 특정 옵션(인원 제한 등) 일괄 조회
    List<SurveyOption> findBySurveyIdInAndKeyAndIsActiveTrue(List<Long> surveyIds, SurveyOptionKey key);
}

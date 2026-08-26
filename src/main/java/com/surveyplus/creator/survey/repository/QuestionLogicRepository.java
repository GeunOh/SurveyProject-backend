package com.surveyplus.creator.survey.repository;

import com.surveyplus.creator.survey.entity.QuestionLogic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuestionLogicRepository extends JpaRepository<QuestionLogic, Long> {

    // 설문 편집기 상세 조회용 - 설문에 속한 모든 질문을 기준으로 하는 로직을 한 번에 조회
    List<QuestionLogic> findBySourceQuestionIdIn(Collection<Long> sourceQuestionIds);

    // 응답 제출 시 다음 문항을 결정하기 위한 조회 - 우선순위는 id(생성 순서) 오름차순
    List<QuestionLogic> findBySourceQuestionIdOrderById(Long sourceQuestionId);

    // 설문 저장(수정) 시 기존 로직을 전부 지우고 요청값으로 다시 채우기 위한 삭제
    void deleteBySourceQuestionIdIn(Collection<Long> sourceQuestionIds);
}

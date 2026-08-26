package com.surveyplus.creator.answer.service;

import com.surveyplus.creator.answer.enums.AnswerStatus;
import com.surveyplus.creator.answer.repository.AnswerSessionRepository;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import com.surveyplus.creator.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 응답 흐름 중 예외를 던지면서 동시에 상태를 남겨야 하는 경우를 위한 컴포넌트
// 호출한 쪽의 트랜잭션이 나중에 롤백되더라도, 여기서 저장한 상태는 독립적인 트랜잭션으로 이미 커밋되어 영향을 받지 않음
@Component
@RequiredArgsConstructor
public class AnswerSessionStatusUpdater {

    private final AnswerSessionRepository answerSessionRepository;
    private final SurveyRepository surveyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markQuotaOutIfInProgress(Long sessionId) {
        answerSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getStatus() == AnswerStatus.PROGRESS) {
                session.markQuotaOut();
            }
        });
    }

    // 진행 중이던 세션이 스스로 정원 도달을 감지한 경우 - 그 세션과 설문 상태를 함께 정원마감으로 확정
    // (설문 상태까지 같이 바꿔야, 신규 진입자도 막히고 종료 페이지에서 다시 조회해도 정원마감으로 보임)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSessionAndSurveyQuotaOut(Long sessionId, Long surveyId) {
        answerSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getStatus() == AnswerStatus.PROGRESS) {
                session.markQuotaOut();
            }
        });

        surveyRepository.findById(surveyId).ifPresent(survey -> {
            if (survey.getSurveyStatus() != SurveyStatus.QUOTA_OUT) {
                survey.changeStatus(SurveyStatus.QUOTA_OUT);
            }
        });
    }
}

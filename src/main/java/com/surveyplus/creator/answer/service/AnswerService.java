package com.surveyplus.creator.answer.service;

import com.surveyplus.creator.answer.dto.request.QuestionDetailRequest;
import com.surveyplus.creator.answer.dto.request.SubmitAnswerRequest;
import com.surveyplus.creator.answer.dto.request.SurveyStartRequest;
import com.surveyplus.creator.answer.dto.response.*;
import com.surveyplus.creator.answer.entity.ResponseAnswer;
import com.surveyplus.creator.answer.entity.ResponseStatus;
import com.surveyplus.creator.answer.exception.AnswerErrorCode;
import com.surveyplus.creator.answer.exception.AnswerException;
import com.surveyplus.creator.answer.repository.SurveyResponseAnswerRepository;
import com.surveyplus.creator.answer.repository.SurveyResponseRepository;
import com.surveyplus.creator.answer.util.SurveyTimeCalculator;
import com.surveyplus.creator.survey.entity.Question;
import com.surveyplus.creator.survey.entity.Survey;
import com.surveyplus.creator.survey.exception.QuestionErrorCode;
import com.surveyplus.creator.survey.exception.QuestionException;
import com.surveyplus.creator.survey.exception.SurveyErrorCode;
import com.surveyplus.creator.survey.exception.SurveyException;
import com.surveyplus.creator.survey.repository.QuestionRepository;
import com.surveyplus.creator.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyResponseAnswerRepository surveyResponseAnswerRepository;

    public SurveyIntroResponse getSurveyIntroAndCreateRandomId(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        String surveyRandomId;
        int retryCount = 0;
        int maxRetry = 5;

        String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        do {
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 24; i++) {
                int index = random.nextInt(CHAR_POOL.length());
                sb.append(CHAR_POOL.charAt(index));
            }
            surveyRandomId = sb.toString();
            retryCount++;

            if (retryCount > maxRetry) {
                throw new AnswerException(AnswerErrorCode.SESSION_ID_GENERATE_FAILED);
            }

        } while (surveyResponseRepository.existsBySurveyIdAndAnswerId(surveyId, surveyRandomId));

        log.info("설문 인트로 랜덤 ID 생성 완료 - SurveyId: {}, RandomId: {}, 시도 횟수: {}회", surveyId, surveyRandomId, retryCount);

        return survey.fromIntro(surveyRandomId);
    }

    @Transactional
    public SurveyStartResponse startSurvey(SurveyStartRequest surveyStartReq) {
        Survey survey = surveyRepository.findById(surveyStartReq.getSurveyId())
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        Long firstQuestionId = null;
        if (survey.getQuestions() != null && !survey.getQuestions().isEmpty()) {
            firstQuestionId = survey.getQuestions().get(0).getId();
        }

        ResponseStatus surveyResponse = surveyStartReq.toEntity();
        surveyResponse.updateQuestionProgress(null, firstQuestionId);


        log.info("설문 응답이 정상적으로 시작되었습니다. SurveyId: {}, AnswerId: {}",
                surveyStartReq.getSurveyId(), surveyStartReq.getAnswerId());

        return surveyResponseRepository.save(surveyResponse).from();
    }

    public QuestionDetailResponse getQuestionDetail(QuestionDetailRequest questionDetailReq) {
        Survey survey = surveyRepository.findById(questionDetailReq.getSurveyId())
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        Question question = questionRepository.findByIdAndSurveyId(questionDetailReq.getQuestionId(), questionDetailReq.getSurveyId())
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));

        List<ResponseAnswer> savedAnswers = surveyResponseAnswerRepository
                .findByQuestionIdAndAnswerIdAndDeletedAtIsNull(questionDetailReq.getQuestionId(), questionDetailReq.getAnswerId());

        List<AnswerResponse> answerResponses = savedAnswers.stream()
                .map(ResponseAnswer::from)
                .toList();

        int estimatedTime = survey.getQuestions() == null ? 0 :
                survey.getQuestions().stream()
                        .filter(q -> q.getOrder() > question.getOrder())
                        .mapToInt(q -> SurveyTimeCalculator.getQuestionSeconds(q.getType()))
                        .sum();

        log.info("설문 문항 상세 조회 완료: surveyId={}, questionId={}, questionTitle={}",
                questionDetailReq.getSurveyId(), questionDetailReq.getQuestionId(), question.getTitle());

        return question.fromDetail(estimatedTime, answerResponses);
    }

    @Transactional
    public SubmitAnswerResponse submitAnswerAndGetNext(SubmitAnswerRequest request) {
        Survey survey = surveyRepository.findByIdWithQuestions(request.getSurveyId())
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        ResponseStatus surveyResponse = surveyResponseRepository
                .findBySurveyIdAndAnswerId(request.getSurveyId(), request.getAnswerId())
                .orElseThrow(() -> new AnswerException(AnswerErrorCode.ANSWER_SESSION_NOT_FOUND));

        // 응답 상태가 "PROGRESS"가 아니면 예외 처리 (이미 완료되었거나 중복 제출된 경우)
        if (!"PROGRESS".equals(surveyResponse.getResponseStatus())) {
            throw new SurveyException(SurveyErrorCode.INVALID_SURVEY_STATUS);
        }

        // 새로 제출하기 전에, 해당 문항에 남아있던 기존 답변들을 소프트 딜리트 처리
        surveyResponseAnswerRepository.softDeleteExistingAnswers(request.getAnswerId(), request.getQuestionId(), LocalDateTime.now());

        // 제출된 답변 엔티티 변환 및 저장
        List<ResponseAnswer> answers = request.toEntities();
        surveyResponseAnswerRepository.saveAll(answers);

        // 다음 문항 정보를 가져오는 메서드
        SubmitAnswerResponse response = findNextQuestionInfo(survey, request.getQuestionId());

        // SurveyResponse 상태 및 진행 문항 업데이트
        if (response.isCompleted()) {
            surveyResponse.complete();
        } else {
            surveyResponse.updateQuestionProgress(request.getQuestionId(), response.getNextQuestionId());
        }

        // 그대로 반환
        return response;
    }

    private SubmitAnswerResponse findNextQuestionInfo(Survey survey, Long currentQuestionId) {
        List<Question> questions = survey.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (q.getId().equals(currentQuestionId)) {
                // 다음 문항이 존재하는 경우
                if (i + 1 < questions.size()) {
                    Question nextQ = questions.get(i + 1);
                    return new SubmitAnswerResponse(nextQ.getId(), nextQ.getOrder(), false);
                }
                break;
            }
        }

        // 다음 문항이 없다면 설문 완료 상태 반환
        return new SubmitAnswerResponse(null, null, true);
    }
}

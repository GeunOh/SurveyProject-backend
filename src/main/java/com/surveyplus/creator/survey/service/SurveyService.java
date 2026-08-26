package com.surveyplus.creator.survey.service;

import com.surveyplus.creator.answer.service.AnswerService;
import com.surveyplus.creator.survey.dto.request.*;
import com.surveyplus.creator.survey.dto.response.HeatmapCellResponse;
import com.surveyplus.creator.survey.dto.response.RecentResponseItem;
import com.surveyplus.creator.survey.dto.response.ResponseTimeStatsResponse;
import com.surveyplus.creator.survey.dto.response.ResponseTrendItem;
import com.surveyplus.creator.survey.dto.response.StatsComparisonResponse;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import com.surveyplus.creator.survey.dto.response.SurveyResponse;
import com.surveyplus.creator.survey.dto.response.SurveyStatsResponse;
import com.surveyplus.creator.survey.entity.*;
import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import com.surveyplus.creator.survey.dto.response.QuestionLogicResponse;
import com.surveyplus.creator.survey.exception.SurveyErrorCode;
import com.surveyplus.creator.survey.exception.SurveyException;
import com.surveyplus.creator.survey.repository.QuestionLogicRepository;
import com.surveyplus.creator.survey.repository.SurveyRepository;
import com.surveyplus.creator.survey.util.ShareTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final QuestionLogicRepository questionLogicRepository;
    private final ShareTokenProvider shareTokenProvider;
    private final AnswerService answerService;

    @Transactional
    public Long createSurvey(SurveySaveRequest surveyRequest, Long memberId) {

        Survey survey = surveyRequest.toEntity(memberId);
        Survey savedSurvey = surveyRepository.save(survey);

        log.info("[설문 생성 완료]: surveyId={}", savedSurvey.getId());

        return savedSurvey.getId();
    }

    public Page<SurveyListResponse> getSurveyList(SurveySearchCondition surveyCondition, Pageable pageable) {
        log.info("[설문 리스트 조회 서비스 실행] - 회원 ID: {}", surveyCondition.getMemberId());

        return surveyRepository.getSurveyList(surveyCondition, pageable);
    }

    // 홈 대시보드 통계 (설문 상태별 수, 전체 응답 수, 평균 응답률)
    public SurveyStatsResponse getSurveyStats(Long memberId) {
        long totalSurveys = surveyRepository.countByMemberIdAndIsDeletedFalse(memberId);
        long activeSurveys = surveyRepository.countByMemberIdAndIsDeletedFalseAndSurveyStatus(memberId, SurveyStatus.ACTIVE);
        long closedSurveys = surveyRepository.countByMemberIdAndIsDeletedFalseAndSurveyStatus(memberId, SurveyStatus.CLOSED);
        long alreadySurveys = surveyRepository.countByMemberIdAndIsDeletedFalseAndSurveyStatus(memberId, SurveyStatus.ALREADY);
        long pausedSurveys = surveyRepository.countByMemberIdAndIsDeletedFalseAndSurveyStatus(memberId, SurveyStatus.PAUSED);
        long totalResponses = answerService.countCompletedResponsesByMemberId(memberId);
        double avgResponseRate = surveyRepository.getAverageResponseRate(memberId);

        return SurveyStatsResponse.builder()
                .totalSurveys(totalSurveys)
                .activeSurveys(activeSurveys)
                .closedSurveys(closedSurveys)
                .alreadySurveys(alreadySurveys)
                .pausedSurveys(pausedSurveys)
                .totalResponses(totalResponses)
                .avgResponseRate(avgResponseRate)
                .build();
    }

    // 통계 분석 - 특정 설문/테스트 데이터 포함 여부로 좁혀서 볼 수 있는 응답 추이
    public List<ResponseTrendItem> getResponseTrend(Long memberId, int days, Long surveyId, boolean includeTestData) {
        return answerService.getResponseTrend(memberId, days, surveyId, includeTestData);
    }

    // 홈 대시보드 최근 응답 현황 (가장 최근에 완료된 응답 N건)
    public List<RecentResponseItem> getRecentResponses(Long memberId, int size) {
        return answerService.getRecentResponses(memberId, size);
    }

    // 통계 분석 - 요일·시간대별 응답 히트맵
    public List<HeatmapCellResponse> getHeatmap(Long memberId, int days, Long surveyId, boolean includeTestData) {
        return answerService.getHeatmap(memberId, days, surveyId, includeTestData);
    }

    // 통계 분석 - 응답 소요시간 분포(평균/중앙값/최대 + 구간별 히스토그램)
    public ResponseTimeStatsResponse getResponseTimeStats(Long memberId, int days, Long surveyId, boolean includeTestData) {
        return answerService.getResponseTimeStats(memberId, days, surveyId, includeTestData);
    }

    // 통계 분석 - "지난 기간 대비" 비교(응답 수/평균 응답률/평균 응답시간)
    public StatsComparisonResponse getStatsComparison(Long memberId, int days, Long surveyId, boolean includeTestData) {
        return answerService.getStatsComparison(memberId, days, surveyId, includeTestData);
    }

    @Transactional
    public void updateSurveyStatus(Long surveyId, SurveyStatusUpdateRequest surveyStatusReq, Long memberId) {
        Survey survey = getSurveyAndValidateOwner(surveyId, memberId);

        survey.changeStatus(surveyStatusReq.getSurveyStatus());

        log.info("[설문 상태 변경 성공] - Survey ID: {}, Status: {}", surveyId, surveyStatusReq );
    }

    public SurveyResponse getSurveyDetail(Long surveyId, Long memberId) {
        log.info("[설문 상세 조회 요청] surveyId={}", surveyId);

        Survey survey = getSurveyAndValidateOwner(surveyId, memberId);

        List<Long> questionIds = survey.getQuestions().stream().map(Question::getId).toList();
        List<QuestionLogicResponse> logicResponses = questionLogicRepository.findBySourceQuestionIdIn(questionIds).stream()
                .map(QuestionLogic::from)
                .toList();

        return survey.from(logicResponses);
    }

    @Transactional
    public void updateSurvey(Long surveyId, SurveySaveRequest surveyReq, Long memberId) {
        log.info("[설문 수정 시작] surveyId={}, memberId={}", surveyId, memberId);

        // 설문 검증
        Survey survey = getSurveyAndValidateOwner(surveyId, memberId);

        // 기본 정보 수정
        survey.updateInfo(surveyReq.getTitle(), surveyReq.getDescription());

        // 설문 옵션 수정 (삭제 없이 수정/추가만 진행)
        updateSurveyOptions(survey, surveyReq.getOptions());

        // 질문 및 보기 목록 업데이트
        updateQuestions(survey, surveyReq.getQuestions());

        // 응답 분기 로직 업데이트 (질문/보기가 전부 실제 id를 가진 뒤에 처리해야 하므로 반드시 updateQuestions 다음에 실행)
        updateQuestionLogics(survey, surveyReq.getLogics());

        // 인원 제한(LIMIT) 옵션이 바뀌었을 수 있으므로 정원마감 상태를 다시 평가
        answerService.reevaluateQuotaStatus(surveyId);

        log.info("[설문 수정 완료] surveyId={}", surveyId);
    }

    private void updateSurveyOptions(Survey survey, List<SurveyOptionRequest> optionReq) {
        if (optionReq == null || optionReq.isEmpty()) {
            return;
        }

        // 기존 옵션들을 Key 기준으로 Map 변환
        Map<SurveyOptionKey, SurveyOption> existingOptionMap = survey.getOptions().stream()
                .collect(Collectors.toMap(SurveyOption::getKey, opt -> opt));

        // 요청 데이터 순회하며 수정
        for (SurveyOptionRequest optReq : optionReq) {
            SurveyOption existingOption = existingOptionMap.get(optReq.getKey());

            if (existingOption != null) {
                existingOption.updateOption(optReq.getValue(), optReq.getIsActive());
                log.info("[옵션 수정] surveyId={}, key=[{}], value=[{}], isActive=[{}]",
                        survey.getId(),
                        optReq.getKey(),
                        optReq.getValue(),
                        optReq.getIsActive());
            } else {
                SurveyOption newOption = optReq.toEntity();
                newOption.assignSurvey(survey);
                survey.getOptions().add(newOption);
                log.info("[옵션 추가] surveyId={}, key=[{}], value=[{}]",
                        survey.getId(),
                        optReq.getKey(),
                        optReq.getValue());
            }
        }

        log.info("[설문 수정 - 옵션 수정 완료] surveyId={}", survey.getId());
    }

    private void updateQuestions(Survey survey, List<SurveyQuestionRequest> questionRequests) {
        if (questionRequests == null) return;

        // 1. 요청으로 들어온 질문 ID 집합 추출
        Set<Long> requestQuestionIds = questionRequests.stream()
                .map(SurveyQuestionRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. [삭제] 요청 목록에 없는 기존 질문 추출 및 삭제 전 상세 로그 기록
        List<Question> deletedQuestions = survey.getQuestions().stream()
                .filter(q -> !requestQuestionIds.contains(q.getId()))
                .toList();

        for (Question deleted : deletedQuestions) {
            log.info("[설문 수정 - 질문 삭제] surveyId={}, deletedQuestionId={}, title=[{}], type=[{}], order=[{}]",
                    survey.getId(),
                    deleted.getId(),
                    deleted.getTitle(),
                    deleted.getType(),
                    deleted.getOrder());
        }

        // 3. 기존 질문 제거 (orphanRemoval=true 시 DB 삭제 쿼리 발행)
        survey.getQuestions().removeIf(q -> !requestQuestionIds.contains(q.getId()));

        // 4. [수정 및 추가]
        for (SurveyQuestionRequest qReq : questionRequests) {
            if (qReq.getId() != null) {
                // [수정] 기존 질문 조회
                Question existingQuestion = survey.getQuestions().stream()
                        .filter(q -> q.getId().equals(qReq.getId()))
                        .findFirst()
                        .orElseThrow(() -> new SurveyException(SurveyErrorCode.QUESTION_NOT_FOUND));

                // 변경 전 값 백업
                String oldTitle = existingQuestion.getTitle();
                var oldType = existingQuestion.getType();
                Integer oldOrder = existingQuestion.getOrder();
                Boolean oldRequired = existingQuestion.getRequired();

                boolean newRequired = qReq.getRequired() != null ? qReq.getRequired() : false;

                // 질문 기본 정보 업데이트
                existingQuestion.update(
                        qReq.getTitle(),
                        qReq.getDescription(),
                        qReq.getType(),
                        qReq.getOrder(),
                        newRequired
                );

                // 질문별 부가 옵션(글자수 제한, 보기 랜덤 정렬 등) 업데이트
                existingQuestion.updateOptions(qReq.getOptions());

                // 하위 보기(Choices) 목록 업데이트
                updateChoices(existingQuestion, qReq.getChoices());

                // 변경 전/후 값 비교 로그
                log.info("[설문 수정 - 질문 수정] surveyId={}, questionId={} | title: [{}] -> [{}], type: [{}] -> [{}], order: [{}] -> [{}], required: [{}] -> [{}]",
                        survey.getId(),
                        existingQuestion.getId(),
                        oldTitle, qReq.getTitle(),
                        oldType, qReq.getType(),
                        oldOrder, qReq.getOrder(),
                        oldRequired, newRequired);

            } else {
                // [추가] 신규 질문 생성 및 연관관계 맺기
                Question newQuestion = qReq.toEntity();
                newQuestion.assignSurvey(survey);

                // 신규 질문에 함께 전달된 보기(Choices)가 있다면 추가 처리
                updateChoices(newQuestion, qReq.getChoices());

                survey.getQuestions().add(newQuestion);

                log.info("[설문 수정 - 질문 추가] surveyId={} | title: [{}], type: [{}], order: [{}]",
                        survey.getId(),
                        newQuestion.getTitle(),
                        newQuestion.getType(),
                        newQuestion.getOrder());
            }
        }

        log.info("[설문 수정 - 질문 업데이트 완료] surveyId={}", survey.getId());
    }

    private void updateChoices(Question question, List<ChoiceRequest> choiceRequests) {
        if (choiceRequests == null) return;

        // 요청으로 들어온 보기 ID 집합 추출
        Set<Long> requestChoiceIds = choiceRequests.stream()
                .map(ChoiceRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // [삭제] 요청 목록에 없는 기존 보기 추출 및 삭제 전 상세 로그 기록
        List<Choice> deletedChoices = question.getChoices().stream()
                .filter(c -> !requestChoiceIds.contains(c.getId()))
                .toList();

        for (Choice deleted : deletedChoices) {
            log.info("[설문 수정 - 보기 삭제] questionId={}, deletedChoiceId={}, text=[{}], order=[{}]",
                    question.getId(),
                    deleted.getId(),
                    deleted.getText(),
                    deleted.getOrder());
        }

        // 기존 보기 제거 (orphanRemoval=true 시 DB 삭제 쿼리 발행)
        question.getChoices().removeIf(c -> !requestChoiceIds.contains(c.getId()));

        // [수정 및 추가]
        for (ChoiceRequest cReq : choiceRequests) {
            if (cReq.getId() != null) {
                // [수정] 기존 보기 조회
                Choice existingChoice = question.getChoices().stream()
                        .filter(c -> c.getId().equals(cReq.getId()))
                        .findFirst()
                        .orElseThrow(() -> new SurveyException(SurveyErrorCode.CHOICE_NOT_FOUND));

                // 변경 전 값 백업
                String oldText = existingChoice.getText();
                Integer oldOrder = existingChoice.getOrder();

                // 보기 기본 정보 업데이트
                existingChoice.update(cReq.getText(), cReq.getOrder());

                // 보기 옵션 업데이트 (JSON 컬럼 통째로 교체)
                existingChoice.updateOptions(cReq.getOptions());

                // 변경 전/후 값 비교 로그
                log.info("[설문 수정 - 보기 수정] questionId={}, choiceId={} | text: [{}] -> [{}], order: [{}] -> [{}]",
                        question.getId(),
                        existingChoice.getId(),
                        oldText, cReq.getText(),
                        oldOrder, cReq.getOrder());

            } else {
                // [추가] 신규 보기 생성 및 연관관계 맺기 (옵션은 toEntity()에서 이미 설정됨)
                Choice newChoice = cReq.toEntity();
                newChoice.assignQuestion(question);

                question.getChoices().add(newChoice);

                log.info("[설문 수정 - 보기 추가] questionId={} | text=[{}], order=[{}]",
                        question.getId(),
                        newChoice.getText(),
                        newChoice.getOrder());
            }
        }

        log.info("[설문 수정 - 보기 업데이트 완료] questionId={} | questionTitle=[{}]", question.getId(), question.getTitle() );
    }

    // 응답 분기 로직 업데이트 - 로직의 id는 다른 테이블에서 참조되지 않으므로(응답 데이터와 무관한 저작 전용 데이터)
    // 질문/보기처럼 id 기준으로 diff하지 않고, 이 설문에 속한 기존 로직을 전부 지운 뒤 요청값을 그대로 다시 저장
    private void updateQuestionLogics(Survey survey, List<QuestionLogicRequest> logicRequests) {
        List<Long> questionIds = survey.getQuestions().stream().map(Question::getId).toList();

        if (!questionIds.isEmpty()) {
            questionLogicRepository.deleteBySourceQuestionIdIn(questionIds);
        }

        if (logicRequests == null || logicRequests.isEmpty()) {
            log.info("[설문 수정 - 로직 업데이트 완료] surveyId={}, 저장된 로직 수=0", survey.getId());
            return;
        }

        // 기준/대상 질문이 실제로 이 설문에 속하는지 검증 (다른 설문의 질문 id가 섞여 들어오는 것을 방지)
        Set<Long> validQuestionIds = new HashSet<>(questionIds);

        List<QuestionLogic> newLogics = logicRequests.stream()
                .filter(req -> validQuestionIds.contains(req.getSourceQuestionId()))
                .filter(req -> !"QUESTION".equals(req.getTargetType()) || validQuestionIds.contains(req.getTargetQuestionId()))
                .map(QuestionLogicRequest::toEntity)
                .toList();

        questionLogicRepository.saveAll(newLogics);

        log.info("[설문 수정 - 로직 업데이트 완료] surveyId={}, 저장된 로직 수={}", survey.getId(), newLogics.size());
    }

    @Transactional
    public void deleteSurvey(Long surveyId, Long memberId) {
        Survey survey = getSurveyAndValidateOwner(surveyId, memberId);

        survey.deleteSurvey();

        log.info("[설문 삭제 완료] - Survey ID: {} 가 성공적으로 삭제되었습니다.", surveyId);
    }

    // 설문 응답 공유 링크(인트로 진입 URL)에 사용할 토큰 발급
    // isTest=true면 설문 상태(준비중 등)와 무관하게 응답 흐름을 끝까지 체험할 수 있는 테스트 링크 토큰
    public String getShareToken(Long surveyId, Long memberId, boolean isTest) {
        Survey survey = getSurveyAndValidateOwner(surveyId, memberId);

        return shareTokenProvider.generate(survey.getId(), isTest);
    }

    private Survey getSurveyAndValidateOwner(Long surveyId, Long memberId) {
        Survey survey = surveyRepository.findByIdAndIsDeletedFalse(surveyId)
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        if (!survey.getMemberId().equals(memberId)) {
            throw new SurveyException(SurveyErrorCode.UNAUTHORIZED_ACTION);
        }
        return survey;
    }
}

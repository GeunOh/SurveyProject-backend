package com.surveyplus.creator.survey.service;

import com.surveyplus.creator.survey.dto.request.*;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import com.surveyplus.creator.survey.dto.response.SurveyResponse;
import com.surveyplus.creator.survey.entity.*;
import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import com.surveyplus.creator.survey.exception.SurveyErrorCode;
import com.surveyplus.creator.survey.exception.SurveyException;
import com.surveyplus.creator.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Long createSurvey(SurveySaveRequest surveyRequest) {

        Survey survey = surveyRequest.toEntity();
        Survey savedSurvey = surveyRepository.save(survey);

        log.info("[설문 생성 완료]: surveyId={}", savedSurvey.getId());

        return savedSurvey.getId();
    }

    public Page<SurveyListResponse> getSurveyList(
            SurveySearchCondition surveyCondition,
            Pageable pageable
    ) {
        log.info("[설문 리스트 조회 서비스 실행] - 회원 ID: {}", surveyCondition.getMemberId());

        return surveyRepository.getSurveyList(surveyCondition, pageable);
    }

    @Transactional
    public void updateSurveyStatus(Long surveyId, SurveyStatusUpdateRequest surveyStatusReq) {
        Survey survey = getSurveyAndValidateOwner(surveyId, surveyStatusReq.getMemberId());

        survey.changeStatus(surveyStatusReq.getSurveyStatus());

        log.info("[설문 상태 변경 성공] - Survey ID: {}, Status: {}", surveyId, surveyStatusReq );
    }

    public SurveyResponse getSurveyDetail(Long surveyId) {
        log.info("[설문 상세 조회 요청] surveyId={}", surveyId);

        Survey survey = surveyRepository.findByIdAndIsDeletedFalse(surveyId)
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        return survey.from();
    }

    @Transactional
    public void updateSurvey(Long surveyId, SurveySaveRequest surveyReq) {
        log.info("[설문 수정 시작] surveyId={}, memberId={}", surveyId, surveyReq.getMemberId());

        // 설문 검증
        Survey survey = getSurveyAndValidateOwner(surveyId, surveyReq.getMemberId());

        // 기본 정보 수정
        survey.updateInfo(surveyReq.getTitle(), surveyReq.getDescription());

        // 설문 옵션 수정 (삭제 없이 수정/추가만 진행)
        updateSurveyOptions(survey, surveyReq.getOptions());

        // 질문 및 보기 목록 업데이트
        updateQuestions(survey, surveyReq.getQuestions());

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

                // 보기 옵션 목록 업데이트
                updateChoiceOptions(existingChoice, cReq.getOptions());

                // 변경 전/후 값 비교 로그
                log.info("[설문 수정 - 보기 수정] questionId={}, choiceId={} | text: [{}] -> [{}], order: [{}] -> [{}]",
                        question.getId(),
                        existingChoice.getId(),
                        oldText, cReq.getText(),
                        oldOrder, cReq.getOrder());

            } else {
                // [추가] 신규 보기 생성 및 연관관계 맺기
                Choice newChoice = cReq.toEntity();
                newChoice.assignQuestion(question);

                // 신규 보기에 함께 전달된 옵션이 있다면 추가 처리
                updateChoiceOptions(newChoice, cReq.getOptions());

                question.getChoices().add(newChoice);

                log.info("[설문 수정 - 보기 추가] questionId={} | text=[{}], order=[{}]",
                        question.getId(),
                        newChoice.getText(),
                        newChoice.getOrder());
            }
        }

        log.info("[설문 수정 - 보기 업데이트 완료] questionId={} | questionTitle=[{}]", question.getId(), question.getTitle() );
    }

    private void updateChoiceOptions(Choice choice, List<ChoiceOptionRequest> optionReqs) {
        if (optionReqs == null) return;

        // 요청으로 들어온 옵션 Key 집합 추출
        Set<String> requestOptionKeys = optionReqs.stream()
                .map(ChoiceOptionRequest::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // [삭제] 요청 목록에 없는 기존 옵션 추출 및 삭제 전 로그 기록
        List<ChoiceOption> deletedOptions = choice.getOptions().stream()
                .filter(opt -> !requestOptionKeys.contains(opt.getKey()))
                .toList();

        for (ChoiceOption deleted : deletedOptions) {
            log.info("[설문 수정 - 보기 옵션 삭제] choiceId={}, deletedKey=[{}], oldValue=[{}]",
                    choice.getId(),
                    deleted.getKey(),
                    deleted.getValue());
        }

        // 기존 옵션 제거 (orphanRemoval=true 시 DB 삭제 쿼리 발행)
        choice.getOptions().removeIf(opt -> !requestOptionKeys.contains(opt.getKey()));

        // 기존 옵션들을 Key 기준 Map 변환
        Map<String, ChoiceOption> existingOptionMap = choice.getOptions().stream()
                .collect(Collectors.toMap(ChoiceOption::getKey, opt -> opt));

        // [수정 및 추가]
        for (ChoiceOptionRequest optReq : optionReqs) {
            ChoiceOption existingOption = existingOptionMap.get(optReq.getKey());

            if (existingOption != null) {
                // 변경 전 값 백업
                String oldValue = existingOption.getValue();

                // 값 수정
                existingOption.updateOption(optReq.getValue());

                // 전/후 값 비교 로그
                log.info("[설문 수정 - 보기 옵션 수정] choiceId={}, key=[{}] | value: [{}] -> [{}]",
                        choice.getId(),
                        optReq.getKey(),
                        oldValue, optReq.getValue()
                        );
            } else {
                // 신규 옵션 추가 (이전 값은 null)
                ChoiceOption newOption = optReq.toEntity();
                newOption.assignChoice(choice);
                choice.getOptions().add(newOption);

                log.info("[설문 수정 - 보기 옵션 추가] choiceId={}, key=[{}] | value=[{}]",
                        choice.getId(),
                        newOption.getKey(),
                        newOption.getValue());
            }
        }

        log.info("[설문 수정 - 보기 옵션 업데이트 완료] choiceId={} | choiceText=[{}]", choice.getId(), choice.getText());
    }


    @Transactional
    public void deleteSurvey(Long surveyId) {
        Survey survey = surveyRepository.findByIdAndIsDeletedFalse(surveyId)
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        survey.deleteSurvey();

        log.info("[설문 삭제 완료] - Survey ID: {} 가 성공적으로 삭제되었습니다.", surveyId);
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

package com.surveyplus.creator.answer.service;

import com.surveyplus.creator.answer.dto.request.AnswerItemRequest;
import com.surveyplus.creator.answer.dto.request.QuestionDetailRequest;
import com.surveyplus.creator.answer.dto.request.SubmitAnswerRequest;
import com.surveyplus.creator.answer.dto.request.SurveyStartRequest;
import com.surveyplus.creator.answer.dto.response.*;
import com.surveyplus.creator.survey.dto.response.DurationBucketItem;
import com.surveyplus.creator.survey.dto.response.HeatmapCellResponse;
import com.surveyplus.creator.survey.dto.response.RecentResponseItem;
import com.surveyplus.creator.survey.dto.response.ResponseTimeStatsResponse;
import com.surveyplus.creator.survey.dto.response.ResponseTrendItem;
import com.surveyplus.creator.survey.dto.response.StatsComparisonResponse;
import com.surveyplus.creator.survey.dto.response.StatsPeriodResponse;
import com.surveyplus.creator.answer.entity.SurveyAnswer;
import com.surveyplus.creator.answer.entity.AnswerSession;
import com.surveyplus.creator.answer.enums.AnswerStatus;
import com.surveyplus.creator.answer.enums.SurveyAnswerType;
import com.surveyplus.creator.answer.exception.AnswerErrorCode;
import com.surveyplus.creator.answer.exception.AnswerException;
import com.surveyplus.creator.answer.repository.SurveyAnswerRepository;
import com.surveyplus.creator.answer.repository.AnswerSessionRepository;
import com.surveyplus.creator.answer.util.SurveyTimeCalculator;
import com.surveyplus.creator.survey.entity.Choice;
import com.surveyplus.creator.survey.entity.LogicCondition;
import com.surveyplus.creator.survey.entity.LogicConditionGroup;
import com.surveyplus.creator.survey.entity.LogicConditionItem;
import com.surveyplus.creator.survey.entity.Question;
import com.surveyplus.creator.survey.entity.QuestionLogic;
import com.surveyplus.creator.survey.entity.Survey;
import com.surveyplus.creator.survey.entity.SurveyOption;
import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import com.surveyplus.creator.survey.exception.QuestionErrorCode;
import com.surveyplus.creator.survey.exception.QuestionException;
import com.surveyplus.creator.survey.exception.SurveyErrorCode;
import com.surveyplus.creator.survey.exception.SurveyException;
import com.surveyplus.creator.survey.repository.QuestionLogicRepository;
import com.surveyplus.creator.survey.repository.QuestionRepository;
import com.surveyplus.creator.survey.repository.SurveyOptionRepository;
import com.surveyplus.creator.survey.repository.SurveyRepository;
import com.surveyplus.creator.survey.util.ShareTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    // 홈 대시보드 응답 추이에 허용할 최대 조회 기간(일)
    private static final int MAX_TREND_DAYS = 90;
    // 홈 대시보드 최근 응답 현황에 허용할 최대 조회 건수
    private static final int MAX_RECENT_RESPONSE_SIZE = 20;

    private final SurveyRepository surveyRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionLogicRepository questionLogicRepository;
    private final AnswerSessionRepository answerSessionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final ShareTokenProvider shareTokenProvider;
    private final AnswerSessionStatusUpdater answerSessionStatusUpdater;

    public SurveyIntroResponse getSurveyIntroAndCreateRandomId(String token) {
        // 서명이 유효하지 않으면(위조/오타 등) 존재하지 않는 설문과 동일하게 처리
        ShareTokenProvider.ParsedToken parsed = shareTokenProvider.parse(token)
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));
        Long surveyId = parsed.surveyId();
        boolean isTest = parsed.isTest();

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        // 진행중이 아니면 세션(randomId)을 만들지 않고, 상태 정보만 담아 그대로 반환
        // (프론트에서 surveyStatus를 보고 종료 안내 페이지로 분기 처리함) - 테스트 링크는 상태 무관하게 계속 진행
        if (survey.getSurveyStatus() != SurveyStatus.ACTIVE && !isTest) {
            return survey.fromIntro(null, false, survey.getSurveyStatus());
        }

        // 응답 기간(PERIOD) 옵션이 설정되어 있고 오늘이 그 범위 밖이면, 실제 DB 상태는 그대로 두고
        // 응답자에게는 상황에 맞는 상태(시작 전=ALREADY, 종료 후=PERIOD_END)로 보이도록 함 - 테스트 링크는 예외
        if (!isTest) {
            SurveyStatus periodStatus = getPeriodOutOfRangeStatus(survey);
            if (periodStatus != null) {
                return survey.fromIntro(null, false, periodStatus);
            }
        }

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

        } while (answerSessionRepository.existsBySurveyIdAndAnswerId(surveyId, surveyRandomId));

        log.info("설문 인트로 랜덤 ID 생성 완료 - SurveyId: {}, RandomId: {}, 시도 횟수: {}회", surveyId, surveyRandomId, retryCount);

        return survey.fromIntro(surveyRandomId, isTest, survey.getSurveyStatus());
    }

    @Transactional
    public SurveyStartResponse startSurvey(SurveyStartRequest surveyStartReq) {
        // isTest 여부를 클라이언트가 신고한 값이 아니라, 서명된 공유 토큰을 서버가 직접 재검증해서 판단
        // (자기 신고 방식이면 누구나 surveyAnswerType=TEST를 보내 상태 검증을 우회할 수 있었음)
        ShareTokenProvider.ParsedToken parsed = shareTokenProvider.parse(surveyStartReq.getToken())
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));
        Long surveyId = parsed.surveyId();
        boolean isTest = parsed.isTest();

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        // 테스트 링크는 설문 상태와 무관하게 시작할 수 있음
        if (!isTest) {
            validateSurveyIsActive(survey);
        }

        Long firstQuestionId = null;
        if (survey.getQuestions() != null && !survey.getQuestions().isEmpty()) {
            firstQuestionId = survey.getQuestions().get(0).getId();
        }

        AnswerSession answerSession = AnswerSession.builder()
                .surveyId(surveyId)
                .answerId(surveyStartReq.getAnswerId())
                .surveyType(isTest ? SurveyAnswerType.TEST : SurveyAnswerType.BANNER)
                .build();
        answerSession.updateQuestionProgress(null, firstQuestionId);

        log.info("설문 응답이 정상적으로 시작되었습니다. SurveyId: {}, AnswerId: {}",
                surveyId, surveyStartReq.getAnswerId());

        return answerSessionRepository.save(answerSession).from();
    }

    public QuestionDetailResponse getQuestionDetail(QuestionDetailRequest questionDetailReq) {
        Survey survey = surveyRepository.findById(questionDetailReq.getSurveyId())
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        AnswerSession answerSession = answerSessionRepository
                .findBySurveyIdAndAnswerId(questionDetailReq.getSurveyId(), questionDetailReq.getAnswerId())
                .orElseThrow(() -> new AnswerException(AnswerErrorCode.ANSWER_SESSION_NOT_FOUND));

        // 이전 버튼/새로고침으로 재조회하는 시점에도 설문이 여전히 진행중인지 확인 (테스트 세션은 예외)
        if (answerSession.getSurveyType() != SurveyAnswerType.TEST) {
            validateSurveyIsActive(survey, answerSession);
        }

        Question question = questionRepository.findByIdAndSurveyId(questionDetailReq.getQuestionId(), questionDetailReq.getSurveyId())
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));

        List<SurveyAnswer> savedAnswers = surveyAnswerRepository
                .findByQuestionIdAndAnswerIdAndDeletedAtIsNull(questionDetailReq.getQuestionId(), questionDetailReq.getAnswerId());

        List<SurveyAnswerResponse> answerResponses = savedAnswers.stream()
                .map(SurveyAnswer::from)
                .toList();

        int estimatedTime = survey.getQuestions() == null ? 0 :
                survey.getQuestions().stream()
                        .filter(q -> q.getOrder() >= question.getOrder())
                        .mapToInt(q -> SurveyTimeCalculator.getQuestionSeconds(q.getType()))
                        .sum();

        log.info("설문 문항 상세 조회 완료: surveyId={}, questionId={}, questionTitle={}",
                questionDetailReq.getSurveyId(), questionDetailReq.getQuestionId(), question.getTitle());

        int totalQuestions = survey.getQuestions() == null ? 0 : survey.getQuestions().size();

        return question.fromDetail(estimatedTime, answerResponses, totalQuestions, getActiveThemeColor(survey));
    }

    // 활성화된 테마(THEME) 옵션 색상값을 반환 (옵션이 없거나 비활성화면 기본 색상)
    private String getActiveThemeColor(Survey survey) {
        return surveyOptionRepository.findBySurveyIdAndKeyAndIsActiveTrue(survey.getId(), SurveyOptionKey.THEME)
                .map(SurveyOption::getValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse("#7B4DFF");
    }

    @Transactional
    public SubmitAnswerResponse submitAnswerAndGetNext(SubmitAnswerRequest request) {
        Survey survey = surveyRepository.findByIdWithQuestions(request.getSurveyId())
                .orElseThrow(() -> new SurveyException(SurveyErrorCode.SURVEY_NOT_FOUND));

        AnswerSession answerSession = answerSessionRepository
                .findBySurveyIdAndAnswerId(request.getSurveyId(), request.getAnswerId())
                .orElseThrow(() -> new AnswerException(AnswerErrorCode.ANSWER_SESSION_NOT_FOUND));

        // 이 세션 자체가 이미 끝난 상태인지부터 확인 (설문이 지금 어떤 상태인지와 무관하게 우선 판단)
        // 프론트가 상황에 맞는 종료 페이지로 보낼 수 있도록 상태별로 다른 에러를 던짐
        if (answerSession.getStatus() == AnswerStatus.END) {
            throw new AnswerException(AnswerErrorCode.ALREADY_COMPLETED);
        }
        if (answerSession.getStatus() != AnswerStatus.PROGRESS) {
            // QUOTAOUT/SCREEN 등 - SVE006과 동일하게 처리해 종료 페이지에서 최신 설문 상태를 다시 보여줌
            throw new SurveyException(SurveyErrorCode.SURVEY_NOT_ACTIVE);
        }

        boolean isTest = answerSession.getSurveyType() == SurveyAnswerType.TEST;
        if (!isTest) {
            validateSurveyIsActive(survey, answerSession);
        }

        // 필수 문항인데 답변이 비어있으면 제출 거부 (선택 문항은 답변 없이 제출 가능)
        Question currentQuestion = survey.getQuestions().stream()
                .filter(q -> q.getId().equals(request.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));

        // 안내문(INFO)은 답변할 보기 자체가 없는 순수 안내 화면이므로, 혹시 옛 데이터 등으로 required가 true로
        // 저장되어 있더라도 필수 답변 검증 대상에서 제외 (프론트 에디터도 안내문 유형은 required 토글을 숨김)
        boolean hasAnswer = request.getAnswer() != null && !request.getAnswer().isEmpty();
        if (!"INFO".equals(currentQuestion.getType()) && Boolean.TRUE.equals(currentQuestion.getRequired()) && !hasAnswer) {
            throw new AnswerException(AnswerErrorCode.REQUIRED_ANSWER_MISSING);
        }

        // 로직 평가에 필요한 이 문항의 현재 보기 id 목록을 미리 읽어둠
        // (바로 아래 softDeleteExistingAnswers가 clearAutomatically=true라 영속성 컨텍스트를 비워버려서,
        //  그 이후에는 currentQuestion.getChoices()의 지연 로딩이 실패함 - LazyInitializationException)
        Set<Long> existingChoiceIds = currentQuestion.getChoices().stream()
                .map(Choice::getId)
                .collect(Collectors.toSet());

        // 새로 제출하기 전에, 해당 문항에 남아있던 기존 답변들을 소프트 딜리트 처리
        surveyAnswerRepository.softDeleteExistingAnswers(request.getAnswerId(), request.getQuestionId(), LocalDateTime.now());

        // 제출된 답변 엔티티 변환 및 저장
        List<SurveyAnswer> answers = request.toEntities();
        surveyAnswerRepository.saveAll(answers);

        // 다음 문항 정보를 가져오는 메서드 (분기 로직이 있으면 우선 적용, 없으면 순서상 다음 문항)
        SubmitAnswerResponse response = findNextQuestionInfo(survey, currentQuestion, existingChoiceIds, request.getAnswer());

        // AnswerSession 상태 및 진행 문항 업데이트
        if (response.isCompleted()) {
            if (response.isScreened()) {
                answerSession.screen();
            } else {
                answerSession.complete();
            }
        } else {
            answerSession.updateQuestionProgress(request.getQuestionId(), response.getNextQuestionId());
        }

        // dirty checking에 의존하지 않고 명시적으로 저장
        // 정원(쿼터) 카운트가 "지금 막 완료된 이 응답"까지 포함해서 세도록, 정원 체크보다 먼저 저장해야 함
        answerSessionRepository.save(answerSession);
        log.info("AnswerSession 저장 완료 - id={}, status={}, endedAt={}", answerSession.getId(), answerSession.getStatus(), answerSession.getEndedAt());

        // 테스트 응답이거나 중도 탈락(스크리닝 아웃)한 응답은 정원 집계에 포함되지 않으므로 건너뜀
        if (response.isCompleted() && !response.isScreened() && !isTest) {
            closeSurveyIfQuotaReached(survey);
        }

        // 그대로 반환
        return response;
    }

    // 대시보드 통계용 - 회원이 소유한 모든 설문에 걸친 완료 응답 수 (테스트 응답 제외)
    public long countCompletedResponsesByMemberId(Long memberId) {
        return answerSessionRepository.countCompletedResponsesByMemberId(memberId, AnswerStatus.END, SurveyAnswerType.TEST);
    }

    // 홈 대시보드 응답 추이 - 최근 N일간 일자별 완료 응답 수 (테스트 응답 제외, 응답 없는 날짜는 0으로 채워서 반환)
    public List<ResponseTrendItem> getResponseTrend(Long memberId, int days) {
        return getResponseTrend(memberId, days, null, false);
    }

    // 통계 분석 - 특정 설문으로 좁혀서 볼 수 있는 버전 (surveyId가 null이면 회원의 전체 설문 기준, 테스트 응답 제외)
    public List<ResponseTrendItem> getResponseTrend(Long memberId, int days, Long surveyId) {
        return getResponseTrend(memberId, days, surveyId, false);
    }

    // 통계 분석 - 테스트 응답 포함 여부까지 선택할 수 있는 버전
    public List<ResponseTrendItem> getResponseTrend(Long memberId, int days, Long surveyId, boolean includeTestData) {
        int clampedDays = Math.min(Math.max(days, 1), MAX_TREND_DAYS);
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(clampedDays - 1L).atStartOfDay();

        List<Long> surveyIds = resolveSurveyIds(memberId, surveyId);
        List<AnswerSession> sessions = surveyIds.isEmpty()
                ? List.of()
                : answerSessionRepository.findBySurveyIdInAndStatusAndSurveyTypeInAndEndedAtGreaterThanEqual(
                        surveyIds, AnswerStatus.END, resolveSurveyTypes(includeTestData), from);

        Map<LocalDate, Long> countByDate = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getEndedAt().toLocalDate(), Collectors.counting()));

        return IntStream.range(0, clampedDays)
                .mapToObj(i -> today.minusDays(clampedDays - 1L - i))
                .map(date -> ResponseTrendItem.builder()
                        .date(date.toString())
                        .count(countByDate.getOrDefault(date, 0L))
                        .build())
                .toList();
    }

    // 홈 대시보드 최근 응답 현황 - 가장 최근에 완료된 응답 N건 (익명 식별키 + 설문 제목)
    public List<RecentResponseItem> getRecentResponses(Long memberId, int size) {
        int clampedSize = Math.min(Math.max(size, 1), MAX_RECENT_RESPONSE_SIZE);

        List<Long> surveyIds = surveyRepository.findIdsByMemberIdAndIsDeletedFalse(memberId);
        if (surveyIds.isEmpty()) {
            return List.of();
        }

        List<AnswerSession> sessions = answerSessionRepository.findBySurveyIdInAndStatusAndSurveyTypeNotOrderByEndedAtDesc(
                surveyIds, AnswerStatus.END, SurveyAnswerType.TEST, PageRequest.of(0, clampedSize));

        Map<Long, String> titleBySurveyId = surveyRepository.findAllById(
                        sessions.stream().map(AnswerSession::getSurveyId).distinct().toList()).stream()
                .collect(Collectors.toMap(Survey::getId, Survey::getTitle));

        return sessions.stream()
                .map(session -> RecentResponseItem.builder()
                        .anonymousId(maskAnswerId(session.getAnswerId()))
                        .surveyTitle(titleBySurveyId.getOrDefault(session.getSurveyId(), ""))
                        .respondedAt(session.getEndedAt())
                        .build())
                .toList();
    }

    // answerId는 세션 시작 시 발급되는 무작위 토큰이라 그 자체로 익명 식별자이지만,
    // 화면에는 앞 10자리만 노출해 전체 값이 그대로 드러나지 않게 함
    private String maskAnswerId(String answerId) {
        if (answerId == null || answerId.length() <= 10) {
            return answerId;
        }
        return answerId.substring(0, 10) + "...";
    }

    // 통계 분석 - 요일·시간대별 응답 히트맵 (includeTestData로 테스트 응답 포함 여부 선택)
    public List<HeatmapCellResponse> getHeatmap(Long memberId, int days, Long surveyId, boolean includeTestData) {
        int clampedDays = Math.min(Math.max(days, 1), MAX_TREND_DAYS);
        LocalDateTime from = LocalDate.now().minusDays(clampedDays - 1L).atStartOfDay();

        List<Long> surveyIds = resolveSurveyIds(memberId, surveyId);
        List<AnswerSession> sessions = surveyIds.isEmpty()
                ? List.of()
                : answerSessionRepository.findBySurveyIdInAndStatusAndSurveyTypeInAndEndedAtGreaterThanEqual(
                        surveyIds, AnswerStatus.END, resolveSurveyTypes(includeTestData), from);

        long[][] grid = new long[7][24]; // day 0=월 ~ 6=일
        for (AnswerSession session : sessions) {
            int day = session.getEndedAt().getDayOfWeek().getValue() - 1;
            int hour = session.getEndedAt().getHour();
            grid[day][hour]++;
        }

        List<HeatmapCellResponse> cells = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 24; hour++) {
                cells.add(HeatmapCellResponse.builder().day(day).hour(hour).count(grid[day][hour]).build());
            }
        }
        return cells;
    }

    // 통계 분석 - 응답 소요시간(startedAt~endedAt) 분포 (평균/중앙값/최대 및 구간별 히스토그램, includeTestData로 테스트 응답 포함 여부 선택)
    public ResponseTimeStatsResponse getResponseTimeStats(Long memberId, int days, Long surveyId, boolean includeTestData) {
        int clampedDays = Math.min(Math.max(days, 1), MAX_TREND_DAYS);
        LocalDateTime from = LocalDate.now().minusDays(clampedDays - 1L).atStartOfDay();

        List<Long> surveyIds = resolveSurveyIds(memberId, surveyId);
        List<Long> durations = surveyIds.isEmpty()
                ? List.of()
                : answerSessionRepository.findBySurveyIdInAndStatusAndSurveyTypeInAndEndedAtGreaterThanEqual(
                                surveyIds, AnswerStatus.END, resolveSurveyTypes(includeTestData), from).stream()
                        .map(s -> Duration.between(s.getStartedAt(), s.getEndedAt()).getSeconds())
                        .filter(seconds -> seconds >= 0)
                        .sorted()
                        .toList();

        long average = durations.isEmpty() ? 0 : Math.round(durations.stream().mapToLong(Long::longValue).average().orElse(0));

        return ResponseTimeStatsResponse.builder()
                .averageSeconds(average)
                .medianSeconds(medianOf(durations))
                .maxSeconds(durations.isEmpty() ? 0 : durations.get(durations.size() - 1))
                .buckets(bucketDurations(durations))
                .build();
    }

    private static final long[] DURATION_BUCKET_UPPER_BOUNDS = {30, 60, 180, 300, 600}; // 초 단위, 마지막 구간("10분 이상")은 상한 없음
    private static final String[] DURATION_BUCKET_LABELS = {"~30초", "30초~1분", "1분~3분", "3분~5분", "5분~10분", "10분 이상"};

    private List<DurationBucketItem> bucketDurations(List<Long> sortedDurations) {
        long[] counts = new long[DURATION_BUCKET_LABELS.length];
        for (long seconds : sortedDurations) {
            int bucketIndex = DURATION_BUCKET_LABELS.length - 1;
            for (int i = 0; i < DURATION_BUCKET_UPPER_BOUNDS.length; i++) {
                if (seconds < DURATION_BUCKET_UPPER_BOUNDS[i]) {
                    bucketIndex = i;
                    break;
                }
            }
            counts[bucketIndex]++;
        }
        return IntStream.range(0, DURATION_BUCKET_LABELS.length)
                .mapToObj(i -> DurationBucketItem.builder().label(DURATION_BUCKET_LABELS[i]).count(counts[i]).build())
                .toList();
    }

    private long medianOf(List<Long> sortedValues) {
        int size = sortedValues.size();
        if (size == 0) return 0;
        return size % 2 == 1
                ? sortedValues.get(size / 2)
                : Math.round((sortedValues.get(size / 2 - 1) + sortedValues.get(size / 2)) / 2.0);
    }

    // 통계 분석 - "지난 기간 대비" 비교용, 선택 기간과 그 직전 동일 길이 기간의 응답 수/평균 응답률/평균 응답시간
    // (includeTestData로 테스트 응답 포함 여부 선택)
    public StatsComparisonResponse getStatsComparison(Long memberId, int days, Long surveyId, boolean includeTestData) {
        int clampedDays = Math.min(Math.max(days, 1), MAX_TREND_DAYS);
        LocalDateTime currentFrom = LocalDate.now().minusDays(clampedDays - 1L).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previousFrom = currentFrom.minusDays(clampedDays);

        List<Long> surveyIds = resolveSurveyIds(memberId, surveyId);
        List<SurveyAnswerType> surveyTypes = resolveSurveyTypes(includeTestData);

        return StatsComparisonResponse.builder()
                .current(computePeriodStats(surveyIds, surveyTypes, currentFrom, now))
                .previous(computePeriodStats(surveyIds, surveyTypes, previousFrom, currentFrom))
                .build();
    }

    private StatsPeriodResponse computePeriodStats(List<Long> surveyIds, List<SurveyAnswerType> surveyTypes, LocalDateTime from, LocalDateTime to) {
        if (surveyIds.isEmpty()) {
            return StatsPeriodResponse.builder().totalResponses(0).avgResponseRate(0).avgResponseSeconds(0).build();
        }

        List<AnswerSession> sessions = answerSessionRepository.findBySurveyIdInAndStatusAndSurveyTypeInAndEndedAtBetween(
                surveyIds, AnswerStatus.END, surveyTypes, from, to);

        long totalResponses = sessions.size();
        long avgResponseSeconds = totalResponses == 0 ? 0 : Math.round(
                sessions.stream()
                        .mapToLong(s -> Duration.between(s.getStartedAt(), s.getEndedAt()).getSeconds())
                        .average()
                        .orElse(0));

        Map<Long, Long> responseCountBySurvey = sessions.stream()
                .collect(Collectors.groupingBy(AnswerSession::getSurveyId, Collectors.counting()));

        Map<Long, Integer> peopleLimitBySurvey = surveyOptionRepository
                .findBySurveyIdInAndKeyAndIsActiveTrue(surveyIds, SurveyOptionKey.LIMIT).stream()
                .collect(Collectors.toMap(
                        option -> option.getSurvey().getId(),
                        this::parsePeopleLimit,
                        (a, b) -> a));

        double responseRateSum = 0;
        for (Long id : surveyIds) {
            int peopleLimit = peopleLimitBySurvey.getOrDefault(id, 0);
            long responseCount = responseCountBySurvey.getOrDefault(id, 0L);
            responseRateSum += peopleLimit > 0 ? Math.round(responseCount * 1000.0 / peopleLimit) / 10.0 : 0.0;
        }
        double avgResponseRate = Math.round(responseRateSum / surveyIds.size() * 10.0) / 10.0;

        return StatsPeriodResponse.builder()
                .totalResponses(totalResponses)
                .avgResponseRate(avgResponseRate)
                .avgResponseSeconds(avgResponseSeconds)
                .build();
    }

    private int parsePeopleLimit(SurveyOption option) {
        try {
            return Integer.parseInt(option.getValue().trim());
        } catch (NumberFormatException ignored) {
            // 숫자로 변환 안 되는 값은 무시하고 인원 제한 없음으로 취급
            return 0;
        }
    }

    // 통계 분석 API들의 공통 대상 설문 목록 계산 - surveyId가 없으면 회원의 전체 설문, 있으면 그 설문이
    // 실제로 이 회원 소유일 때만 단건 목록으로 반환 (소유가 아니면 다른 회원 데이터 노출을 막기 위해 빈 목록)
    private List<Long> resolveSurveyIds(Long memberId, Long surveyId) {
        List<Long> memberSurveyIds = surveyRepository.findIdsByMemberIdAndIsDeletedFalse(memberId);
        if (surveyId == null) {
            return memberSurveyIds;
        }
        return memberSurveyIds.contains(surveyId) ? List.of(surveyId) : List.of();
    }

    // 통계 분석 API들의 "테스트 데이터 포함" 토글 처리 - 꺼져 있으면 실제 응답만, 켜져 있으면 테스트 응답까지 포함
    private List<SurveyAnswerType> resolveSurveyTypes(boolean includeTestData) {
        return includeTestData
                ? List.of(SurveyAnswerType.BANNER, SurveyAnswerType.LIST, SurveyAnswerType.TEST)
                : List.of(SurveyAnswerType.BANNER, SurveyAnswerType.LIST);
    }

    // 인원 제한(LIMIT) 옵션이 바뀌었을 때(값 변경/켜짐/꺼짐) 설문 상태를 다시 평가
    // - ACTIVE인데 이미 정원을 넘겼으면 QUOTA_OUT으로 전환
    // - QUOTA_OUT인데 더 이상 정원을 넘기지 않으면(제한 상향/해제) ACTIVE로 복귀
    // - PAUSED/CLOSED/ALREADY 등 창작자가 직접 선택한 상태는 건드리지 않음
    @Transactional
    public void reevaluateQuotaStatus(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) {
            return;
        }

        SurveyStatus currentStatus = survey.getSurveyStatus();
        if (currentStatus != SurveyStatus.ACTIVE && currentStatus != SurveyStatus.QUOTA_OUT) {
            return;
        }

        boolean quotaFull = isQuotaFull(survey);

        if (quotaFull && currentStatus == SurveyStatus.ACTIVE) {
            log.info("[인원제한 변경으로 정원마감 전환] surveyId={}", surveyId);
            survey.changeStatus(SurveyStatus.QUOTA_OUT);
            surveyRepository.save(survey);
        } else if (!quotaFull && currentStatus == SurveyStatus.QUOTA_OUT) {
            log.info("[인원제한 변경으로 정원마감 해제] surveyId={}", surveyId);
            survey.changeStatus(SurveyStatus.ACTIVE);
            surveyRepository.save(survey);
        }
    }

    private void validateSurveyIsActive(Survey survey) {
        if (survey.getSurveyStatus() != SurveyStatus.ACTIVE) {
            throw new SurveyException(SurveyErrorCode.SURVEY_NOT_ACTIVE);
        }
        if (getPeriodOutOfRangeStatus(survey) != null) {
            throw new SurveyException(SurveyErrorCode.SURVEY_NOT_ACTIVE);
        }
    }

    // 이미 시작된 세션이 있는 상태에서 설문이 더 이상 진행중이 아님을 확인할 때 사용
    // 정원마감(QUOTA_OUT) 때문에 막힌 응답중(PROGRESS) 세션은 QUOTAOUT으로 남겨 실제로 무슨 일이 있었는지 구분할 수 있게 함
    private void validateSurveyIsActive(Survey survey, AnswerSession session) {
        log.info("[진행중 세션 상태 확인] surveyId={}, surveyStatus={}, sessionId={}, sessionStatus={}",
                survey.getId(), survey.getSurveyStatus(), session.getId(), session.getStatus());

        if (survey.getSurveyStatus() == SurveyStatus.ACTIVE) {
            // 응답 기간(PERIOD) 옵션이 설정되어 있으면 매 요청마다 오늘 날짜가 범위 안인지 직접 확인
            // (날짜는 이벤트 없이도 자연히 지나가므로, DB 상태를 바꿔두지 않고 매번 실시간으로 판단)
            if (getPeriodOutOfRangeStatus(survey) != null) {
                log.info("[응답 기간 아님] surveyId={}, sessionId={}", survey.getId(), session.getId());
                throw new SurveyException(SurveyErrorCode.SURVEY_NOT_ACTIVE);
            }

            // 설문 상태가 아직 QUOTA_OUT으로 안 바뀌었어도, 이 세션이 직접 정원을 확인
            // (다른 세션의 완료 처리가 먼저 설문 상태를 바꿔줄 때까지 기다리지 않고, 매 요청마다 스스로 판단)
            if (session.getStatus() == AnswerStatus.PROGRESS && isQuotaFull(survey)) {
                log.info("[개별 세션 정원마감] surveyId={}, sessionId={}", survey.getId(), session.getId());
                // 세션뿐 아니라 설문 상태도 같이 정원마감으로 확정해야, 신규 진입 차단 + 종료 페이지 재조회가 일관되게 동작함
                answerSessionStatusUpdater.markSessionAndSurveyQuotaOut(session.getId(), survey.getId());
                throw new SurveyException(SurveyErrorCode.SURVEY_NOT_ACTIVE);
            }
            return;
        }

        if (survey.getSurveyStatus() == SurveyStatus.QUOTA_OUT && session.getStatus() == AnswerStatus.PROGRESS) {
            // 이 트랜잭션은 곧 예외로 롤백되므로, 독립된 트랜잭션(REQUIRES_NEW)에서 별도로 커밋
            answerSessionStatusUpdater.markQuotaOutIfInProgress(session.getId());
        }

        throw new SurveyException(SurveyErrorCode.SURVEY_NOT_ACTIVE);
    }

    // 인원 제한 옵션이 켜져있고 완료된 응답 수가 정원 이상인지 확인
    private boolean isQuotaFull(Survey survey) {
        Integer peopleLimit = getActivePeopleLimit(survey);
        if (peopleLimit == null) {
            return false;
        }

        long completedCount = answerSessionRepository.countBySurveyIdAndStatusAndSurveyTypeNot(
                survey.getId(), AnswerStatus.END, SurveyAnswerType.TEST);
        log.info("[정원 체크] surveyId={}, peopleLimit={}, completedCount={}", survey.getId(), peopleLimit, completedCount);

        return completedCount >= peopleLimit;
    }

    // 완료된 응답 수가 정원에 도달했으면 설문을 정원마감(QUOTA_OUT) 상태로 전환 (신규 방문자 진입을 막기 위함)
    private void closeSurveyIfQuotaReached(Survey survey) {
        if (!isQuotaFull(survey)) {
            return;
        }

        log.info("설문 정원 도달로 자동 종료 처리: surveyId={}", survey.getId());
        survey.changeStatus(SurveyStatus.QUOTA_OUT);
        // softDeleteExistingAnswers(clearAutomatically=true) 호출 이후라 survey가 준영속 상태 - dirty checking에 기대지 않고 명시적으로 저장
        surveyRepository.save(survey);
    }

    // 활성화된 인원 제한(LIMIT) 옵션 값을 정수로 반환 (옵션이 없거나 비활성화면 null)
    // survey.getOptions()를 직접 lazy-load 하지 않고 별도 쿼리로 조회 (questions와 함께 즉시 로딩된 survey와 얽히지 않도록)
    private Integer getActivePeopleLimit(Survey survey) {
        return surveyOptionRepository.findBySurveyIdAndKeyAndIsActiveTrue(survey.getId(), SurveyOptionKey.LIMIT)
                .map(SurveyOption::getValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value.trim());
                    } catch (NumberFormatException e) {
                        log.warn("인원 제한 옵션 값을 숫자로 변환하지 못했습니다: surveyId={}, value={}", survey.getId(), value);
                        return null;
                    }
                })
                .orElse(null);
    }

    // 활성화된 응답 기간(PERIOD) 옵션이 설정되어 있고 오늘이 그 범위 밖이면, 응답자에게 보여줄 상태(시작 전=ALREADY, 종료 후=PERIOD_END)를 반환
    // 옵션이 없거나 오늘이 범위 안이면 null (제한 없음)
    private SurveyStatus getPeriodOutOfRangeStatus(Survey survey) {
        String value = surveyOptionRepository.findBySurveyIdAndKeyAndIsActiveTrue(survey.getId(), SurveyOptionKey.PERIOD)
                .map(SurveyOption::getValue)
                .orElse(null);

        if (value == null) {
            return null;
        }

        String[] parts = value.split("~");
        if (parts.length != 2) {
            return null;
        }

        try {
            LocalDate from = LocalDate.parse(parts[0].trim());
            LocalDate to = LocalDate.parse(parts[1].trim());
            LocalDate today = LocalDate.now();

            if (today.isBefore(from)) {
                return SurveyStatus.ALREADY;
            }
            if (today.isAfter(to)) {
                return SurveyStatus.PERIOD_END;
            }
            return null;
        } catch (Exception e) {
            log.warn("응답 기간 옵션 값을 날짜로 변환하지 못했습니다: surveyId={}, value={}", survey.getId(), value);
            return null;
        }
    }

    private SubmitAnswerResponse findNextQuestionInfo(Survey survey, Question currentQuestion, Set<Long> existingChoiceIds, List<AnswerItemRequest> submittedAnswers) {
        // 이 문항을 기준으로 하는 분기 로직이 있으면(생성 순서=id 오름차순으로) 먼저 확인
        List<QuestionLogic> logics = questionLogicRepository.findBySourceQuestionIdOrderById(currentQuestion.getId());

        if (!logics.isEmpty()) {
            Set<Long> selectedChoiceIds = submittedAnswers == null ? Set.of() : submittedAnswers.stream()
                    .map(AnswerItemRequest::getChoiceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 값 비교 조건(단답형/장문형/별점형)에 쓸, 제출된 답변의 실제 텍스트 값
            // - 단답형(A01)처럼 입력 필드가 여러 개면 choiceId(필드 id)로 구분
            // - 장문형/별점형처럼 필드가 하나뿐이면 choiceId=0으로 고정되어 내려오므로 키 0으로 조회
            Map<Long, String> submittedTextByChoiceId = submittedAnswers == null ? Map.of() : submittedAnswers.stream()
                    .filter(a -> a.getChoiceId() != null)
                    .collect(Collectors.toMap(AnswerItemRequest::getChoiceId, a -> a.getText() == null ? "" : a.getText(), (a, b) -> a));

            for (QuestionLogic logic : logics) {
                if (!evaluateLogic(logic, selectedChoiceIds, submittedTextByChoiceId, existingChoiceIds)) {
                    continue;
                }

                if ("END".equals(logic.getTargetType()) || "SCREEN".equals(logic.getTargetType())) {
                    boolean screened = "SCREEN".equals(logic.getTargetType());
                    log.info("[분기 로직 적용 - {}] surveyId={}, logicId={}, sourceQuestionId={}",
                            screened ? "중도 탈락" : "설문 종료", survey.getId(), logic.getId(), currentQuestion.getId());
                    return new SubmitAnswerResponse(null, null, true, screened);
                }

                Question targetQuestion = survey.getQuestions().stream()
                        .filter(q -> q.getId().equals(logic.getTargetQuestionId()))
                        .findFirst()
                        .orElse(null);

                // 이동 대상 질문이 삭제되어 더 이상 존재하지 않으면 이 로직은 건너뛰고 다음 순위 로직/기본 순서로 폴백
                if (targetQuestion == null) {
                    log.warn("[분기 로직 무시 - 대상 질문 없음] surveyId={}, logicId={}, targetQuestionId={}",
                            survey.getId(), logic.getId(), logic.getTargetQuestionId());
                    continue;
                }

                log.info("[분기 로직 적용 - 질문 이동] surveyId={}, logicId={}, sourceQuestionId={}, targetQuestionId={}",
                        survey.getId(), logic.getId(), currentQuestion.getId(), targetQuestion.getId());
                return new SubmitAnswerResponse(targetQuestion.getId(), targetQuestion.getOrder(), false, false);
            }
        }

        // 매칭되는 로직이 없으면 기존과 동일하게 순서상 다음 문항으로 이동
        List<Question> questions = survey.getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (q.getId().equals(currentQuestion.getId())) {
                // 다음 문항이 존재하는 경우
                if (i + 1 < questions.size()) {
                    Question nextQ = questions.get(i + 1);
                    return new SubmitAnswerResponse(nextQ.getId(), nextQ.getOrder(), false, false);
                }
                break;
            }
        }

        // 다음 문항이 없다면 설문 완료 상태 반환
        return new SubmitAnswerResponse(null, null, true, false);
    }

    // 로직 하나가 이번 제출로 충족됐는지 판정 (그룹 간 결합은 condition.operator, 그룹 내 조건 간 결합은 group.operator)
    private boolean evaluateLogic(QuestionLogic logic, Set<Long> selectedChoiceIds, Map<Long, String> submittedTextByChoiceId, Set<Long> existingChoiceIds) {
        LogicCondition condition = logic.getConditionJson();
        if (condition == null || condition.getGroups() == null || condition.getGroups().isEmpty()) {
            return false;
        }

        List<Boolean> groupResults = condition.getGroups().stream()
                .map(group -> evaluateGroup(group, selectedChoiceIds, submittedTextByChoiceId, existingChoiceIds))
                .toList();

        return "AND".equals(condition.getOperator())
                ? groupResults.stream().allMatch(Boolean::booleanValue)
                : groupResults.stream().anyMatch(Boolean::booleanValue);
    }

    // 그룹 하나가 충족됐는지 판정. 삭제된 보기/입력 필드를 참조하는 조건은 제외하고 검증하되,
    // 그 결과 살아있는 조건이 하나도 안 남으면(조건이 참조하던 보기/필드가 전부 삭제됨) 영원히 통과할 수 없도록 false 처리
    private boolean evaluateGroup(LogicConditionGroup group, Set<Long> selectedChoiceIds, Map<Long, String> submittedTextByChoiceId, Set<Long> existingChoiceIds) {
        if (group.getItems() == null) {
            return false;
        }

        List<Boolean> liveResults = group.getItems().stream()
                .filter(item -> item.getChoiceId() == null || existingChoiceIds.contains(item.getChoiceId()))
                .map(item -> evaluateConditionItem(item, selectedChoiceIds, submittedTextByChoiceId))
                .toList();

        if (liveResults.isEmpty()) {
            return false;
        }

        return "AND".equals(group.getOperator())
                ? liveResults.stream().allMatch(Boolean::booleanValue)
                : liveResults.stream().anyMatch(Boolean::booleanValue);
    }

    // 조건 항목 하나가 충족됐는지 판정
    // - compare가 없으면(A03/A04) 해당 보기를 선택했는지 여부
    // - compare가 있으면(A01/A02/A05/A06) 제출된 값과 비교값을 연산자에 따라 비교
    //   (A01처럼 입력 필드가 여러 개면 choiceId로 어느 필드인지 구분, 필드가 하나뿐이면 choiceId=0으로 조회)
    private boolean evaluateConditionItem(LogicConditionItem item, Set<Long> selectedChoiceIds, Map<Long, String> submittedTextByChoiceId) {
        if (item.getCompare() == null) {
            return item.getChoiceId() != null && selectedChoiceIds.contains(item.getChoiceId());
        }

        Long key = item.getChoiceId() != null ? item.getChoiceId() : 0L;
        return compareValues(item.getCompare(), submittedTextByChoiceId.get(key), item.getValue());
    }

    // 값 비교 연산자 처리
    // - EQ: 양쪽 다 숫자로 해석되면 숫자 비교, 아니면 문자열(공백 제거) 비교
    // - GTE/GT/LTE/LT: 숫자 비교만 지원 (숫자로 해석 안 되면 매칭 실패)
    // - CONTAINS: 문자열 포함 여부
    private boolean compareValues(String compare, String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }

        return switch (compare) {
            case "CONTAINS" -> actual.contains(expected);
            case "EQ" -> {
                Double actualNum = tryParseDouble(actual);
                Double expectedNum = tryParseDouble(expected);
                if (actualNum != null && expectedNum != null) {
                    yield actualNum.doubleValue() == expectedNum.doubleValue();
                }
                yield actual.trim().equals(expected.trim());
            }
            case "GTE" -> {
                Double actualNum = tryParseDouble(actual);
                Double expectedNum = tryParseDouble(expected);
                yield actualNum != null && expectedNum != null && actualNum >= expectedNum;
            }
            case "GT" -> {
                Double actualNum = tryParseDouble(actual);
                Double expectedNum = tryParseDouble(expected);
                yield actualNum != null && expectedNum != null && actualNum > expectedNum;
            }
            case "LTE" -> {
                Double actualNum = tryParseDouble(actual);
                Double expectedNum = tryParseDouble(expected);
                yield actualNum != null && expectedNum != null && actualNum <= expectedNum;
            }
            case "LT" -> {
                Double actualNum = tryParseDouble(actual);
                Double expectedNum = tryParseDouble(expected);
                yield actualNum != null && expectedNum != null && actualNum < expectedNum;
            }
            default -> false;
        };
    }

    private Double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

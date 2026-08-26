package com.surveyplus.creator.survey.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.surveyplus.creator.answer.entity.QAnswerSession;
import com.surveyplus.creator.answer.enums.AnswerStatus;
import com.surveyplus.creator.answer.enums.SurveyAnswerType;
import com.surveyplus.creator.survey.dto.request.SurveySearchCondition;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import com.surveyplus.creator.survey.entity.QSurvey;
import com.surveyplus.creator.survey.entity.QSurveyOption;
import com.surveyplus.creator.survey.enums.SurveyOptionKey;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SurveyQueryRepositoryImpl implements SurveyQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SurveyListResponse> getSurveyList(SurveySearchCondition condition, Pageable pageable) {

        QSurvey survey = QSurvey.survey;

        // 1. 페이지에 해당하는 설문 기본 정보 조회
        List<Tuple> rows = queryFactory
                .select(survey.id, survey.title, survey.description, survey.surveyStatus)
                .from(survey)
                .where(
                        survey.isDeleted.isFalse(),
                        memberIdEq(condition.getMemberId()),
                        statusEq(condition.getStatus()),
                        keywordContains(condition.getKeyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(survey.id.desc())
                .fetch();

        List<Long> surveyIds = rows.stream().map(row -> row.get(survey.id)).toList();

        // 2. 완료된 응답 수를 설문 ID별로 일괄 조회 (테스트 응답은 제외)
        Map<Long, Long> responseCountMap = getResponseCountMap(surveyIds);

        // 3. 인원 제한(LIMIT)/응답 기간(PERIOD) 옵션을 설문 ID별로 일괄 조회
        Map<Long, List<Tuple>> optionsBySurveyId = getActiveOptionsMap(surveyIds);

        List<SurveyListResponse> content = rows.stream()
                .map(row -> toSurveyListResponse(row, survey, responseCountMap, optionsBySurveyId))
                .toList();

        // 4. 전체 카운트 쿼리 (페이징 총 개수 계산)
        Long total = queryFactory
                .select(survey.count())
                .from(survey)
                .where(
                        survey.isDeleted.isFalse(),
                        memberIdEq(condition.getMemberId()),
                        statusEq(condition.getStatus()),
                        keywordContains(condition.getKeyword())
                )
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;

        return new PageImpl<>(content, pageable, totalCount);
    }

    private Map<Long, Long> getResponseCountMap(List<Long> surveyIds) {
        if (surveyIds.isEmpty()) {
            return Map.of();
        }

        QAnswerSession answerSession = QAnswerSession.answerSession;

        return queryFactory
                .select(answerSession.surveyId, answerSession.id.count())
                .from(answerSession)
                .where(
                        answerSession.surveyId.in(surveyIds),
                        answerSession.status.eq(AnswerStatus.END),
                        answerSession.surveyType.ne(SurveyAnswerType.TEST)
                )
                .groupBy(answerSession.surveyId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        t -> t.get(answerSession.surveyId),
                        t -> t.get(answerSession.id.count())
                ));
    }

    private Map<Long, List<Tuple>> getActiveOptionsMap(List<Long> surveyIds) {
        if (surveyIds.isEmpty()) {
            return Map.of();
        }

        QSurveyOption option = QSurveyOption.surveyOption;

        return queryFactory
                .select(option.survey.id, option.key, option.value)
                .from(option)
                .where(
                        option.survey.id.in(surveyIds),
                        option.isActive.isTrue(),
                        option.key.in(SurveyOptionKey.LIMIT, SurveyOptionKey.PERIOD)
                )
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(t -> t.get(option.survey.id)));
    }

    private SurveyListResponse toSurveyListResponse(
            Tuple row,
            QSurvey survey,
            Map<Long, Long> responseCountMap,
            Map<Long, List<Tuple>> optionsBySurveyId
    ) {
        QSurveyOption option = QSurveyOption.surveyOption;

        Long id = row.get(survey.id);
        SurveyStatus status = row.get(survey.surveyStatus);

        int responseCount = responseCountMap.getOrDefault(id, 0L).intValue();

        int peopleLimit = 0;
        String surveyDateLimit = "";
        for (Tuple optionRow : optionsBySurveyId.getOrDefault(id, List.of())) {
            SurveyOptionKey key = optionRow.get(option.key);
            String value = optionRow.get(option.value);
            if (value == null) {
                continue;
            }

            if (key == SurveyOptionKey.LIMIT) {
                try {
                    peopleLimit = Integer.parseInt(value.trim());
                } catch (NumberFormatException ignored) {
                    // 숫자로 변환 안 되는 값은 무시하고 인원 제한 없음으로 취급
                }
            } else if (key == SurveyOptionKey.PERIOD) {
                surveyDateLimit = value;
            }
        }

        // 인원 제한이 설정된 경우에만 응답률 계산 (소수 첫째 자리까지)
        double responseRate = peopleLimit > 0
                ? Math.round(responseCount * 1000.0 / peopleLimit) / 10.0
                : 0.0;

        return SurveyListResponse.builder()
                .id(id)
                .title(row.get(survey.title))
                .description(row.get(survey.description))
                .surveyStatus(status)
                .responseCount(responseCount)
                .targetCount(peopleLimit)
                .responseRate(responseRate)
                .peopleLimit(peopleLimit)
                .surveyDateLimit(surveyDateLimit)
                .build();
    }

    @Override
    public double getAverageResponseRate(Long memberId) {
        QSurvey survey = QSurvey.survey;

        List<Long> surveyIds = queryFactory
                .select(survey.id)
                .from(survey)
                .where(survey.isDeleted.isFalse(), survey.memberId.eq(memberId))
                .fetch();

        if (surveyIds.isEmpty()) {
            return 0.0;
        }

        Map<Long, Long> responseCountMap = getResponseCountMap(surveyIds);
        Map<Long, List<Tuple>> optionsBySurveyId = getActiveOptionsMap(surveyIds);
        QSurveyOption option = QSurveyOption.surveyOption;

        double sum = 0.0;
        for (Long id : surveyIds) {
            int responseCount = responseCountMap.getOrDefault(id, 0L).intValue();

            int peopleLimit = 0;
            for (Tuple optionRow : optionsBySurveyId.getOrDefault(id, List.of())) {
                if (optionRow.get(option.key) != SurveyOptionKey.LIMIT) {
                    continue;
                }
                String value = optionRow.get(option.value);
                if (value == null) {
                    continue;
                }
                try {
                    peopleLimit = Integer.parseInt(value.trim());
                } catch (NumberFormatException ignored) {
                    // 숫자로 변환 안 되는 값은 무시하고 인원 제한 없음으로 취급
                }
            }

            sum += peopleLimit > 0 ? Math.round(responseCount * 1000.0 / peopleLimit) / 10.0 : 0.0;
        }

        return Math.round(sum / surveyIds.size() * 10.0) / 10.0;
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? QSurvey.survey.memberId.eq(memberId) : null;
    }

    private BooleanExpression statusEq(SurveyStatus status) {
        return status != null ? QSurvey.survey.surveyStatus.eq(status) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? QSurvey.survey.title.containsIgnoreCase(keyword) : null;
    }
}

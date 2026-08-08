package com.surveyplus.creator.survey.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.surveyplus.creator.survey.dto.request.SurveySearchCondition;
import com.surveyplus.creator.survey.dto.response.SurveyListResponse;
import com.surveyplus.creator.survey.entity.QSurvey;
import com.surveyplus.creator.survey.enums.SurveyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class SurveyQueryRepositoryImpl implements SurveyQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SurveyListResponse> getSurveyList(SurveySearchCondition condition, Pageable pageable) {

        QSurvey survey = QSurvey.survey;

        // 1. 컨텐츠 조회 쿼리 (Survey 엔티티 기준)
        List<SurveyListResponse> content = queryFactory
                .select(Projections.constructor(SurveyListResponse.class,
                        survey.id,
                        survey.title,
                        survey.description,
                        survey.surveyStatus,
                        Expressions.constant(0), // responseCount (필요시 서브쿼리 또는 별도 계산 로직 추가)
                        Expressions.constant(0), // targetCount
                        Expressions.constant(0.0), // responseRate
                        Expressions.constant(0), // peopleLimit (옵션 파싱 전 단계 기본값)
                        Expressions.constant("") // surveyDateLimit
                ))
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

        // 2. 전체 카운트 쿼리 (페이징 총 개수 계산)
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

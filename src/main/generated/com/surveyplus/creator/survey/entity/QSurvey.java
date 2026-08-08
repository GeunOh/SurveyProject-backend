package com.surveyplus.creator.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurvey is a Querydsl query type for Survey
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurvey extends EntityPathBase<Survey> {

    private static final long serialVersionUID = 57797920L;

    public static final QSurvey survey = new QSurvey("survey");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final ListPath<SurveyOption, QSurveyOption> options = this.<SurveyOption, QSurveyOption>createList("options", SurveyOption.class, QSurveyOption.class, PathInits.DIRECT2);

    public final ListPath<Question, QQuestion> questions = this.<Question, QQuestion>createList("questions", Question.class, QQuestion.class, PathInits.DIRECT2);

    public final EnumPath<com.surveyplus.creator.survey.enums.SurveyStatus> surveyStatus = createEnum("surveyStatus", com.surveyplus.creator.survey.enums.SurveyStatus.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QSurvey(String variable) {
        super(Survey.class, forVariable(variable));
    }

    public QSurvey(Path<? extends Survey> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSurvey(PathMetadata metadata) {
        super(Survey.class, metadata);
    }

}


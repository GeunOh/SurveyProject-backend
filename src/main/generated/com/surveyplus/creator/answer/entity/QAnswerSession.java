package com.surveyplus.creator.answer.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAnswerSession is a Querydsl query type for AnswerSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAnswerSession extends EntityPathBase<AnswerSession> {

    private static final long serialVersionUID = 746677902L;

    public static final QAnswerSession answerSession = new QAnswerSession("answerSession");

    public final StringPath answerId = createString("answerId");

    public final DateTimePath<java.time.LocalDateTime> endedAt = createDateTime("endedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> prevQuestionId = createNumber("prevQuestionId", Long.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.surveyplus.creator.answer.enums.AnswerStatus> status = createEnum("status", com.surveyplus.creator.answer.enums.AnswerStatus.class);

    public final NumberPath<Long> surveyId = createNumber("surveyId", Long.class);

    public final EnumPath<com.surveyplus.creator.answer.enums.SurveyAnswerType> surveyType = createEnum("surveyType", com.surveyplus.creator.answer.enums.SurveyAnswerType.class);

    public QAnswerSession(String variable) {
        super(AnswerSession.class, forVariable(variable));
    }

    public QAnswerSession(Path<? extends AnswerSession> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAnswerSession(PathMetadata metadata) {
        super(AnswerSession.class, metadata);
    }

}


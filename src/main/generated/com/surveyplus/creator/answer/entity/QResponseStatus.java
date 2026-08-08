package com.surveyplus.creator.answer.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResponseStatus is a Querydsl query type for ResponseStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResponseStatus extends EntityPathBase<ResponseStatus> {

    private static final long serialVersionUID = -303337283L;

    public static final QResponseStatus responseStatus1 = new QResponseStatus("responseStatus1");

    public final StringPath answerId = createString("answerId");

    public final DateTimePath<java.time.LocalDateTime> endedAt = createDateTime("endedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> prevQuestionId = createNumber("prevQuestionId", Long.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final StringPath responseStatus = createString("responseStatus");

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> surveyId = createNumber("surveyId", Long.class);

    public final EnumPath<com.surveyplus.creator.answer.enums.SurveyAnswerType> surveyType = createEnum("surveyType", com.surveyplus.creator.answer.enums.SurveyAnswerType.class);

    public QResponseStatus(String variable) {
        super(ResponseStatus.class, forVariable(variable));
    }

    public QResponseStatus(Path<? extends ResponseStatus> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResponseStatus(PathMetadata metadata) {
        super(ResponseStatus.class, metadata);
    }

}


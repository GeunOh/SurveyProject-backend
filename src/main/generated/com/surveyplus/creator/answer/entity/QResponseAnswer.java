package com.surveyplus.creator.answer.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResponseAnswer is a Querydsl query type for ResponseAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResponseAnswer extends EntityPathBase<ResponseAnswer> {

    private static final long serialVersionUID = -823664503L;

    public static final QResponseAnswer responseAnswer = new QResponseAnswer("responseAnswer");

    public final StringPath answerId = createString("answerId");

    public final StringPath answerText = createString("answerText");

    public final NumberPath<Long> choiceId = createNumber("choiceId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public QResponseAnswer(String variable) {
        super(ResponseAnswer.class, forVariable(variable));
    }

    public QResponseAnswer(Path<? extends ResponseAnswer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResponseAnswer(PathMetadata metadata) {
        super(ResponseAnswer.class, metadata);
    }

}


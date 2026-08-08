package com.surveyplus.creator.answer.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSurveyAnswer is a Querydsl query type for SurveyAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyAnswer extends EntityPathBase<SurveyAnswer> {

    private static final long serialVersionUID = 1357725538L;

    public static final QSurveyAnswer surveyAnswer = new QSurveyAnswer("surveyAnswer");

    public final StringPath answerId = createString("answerId");

    public final StringPath answerText = createString("answerText");

    public final NumberPath<Long> choiceId = createNumber("choiceId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> questionId = createNumber("questionId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public QSurveyAnswer(String variable) {
        super(SurveyAnswer.class, forVariable(variable));
    }

    public QSurveyAnswer(Path<? extends SurveyAnswer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSurveyAnswer(PathMetadata metadata) {
        super(SurveyAnswer.class, metadata);
    }

}


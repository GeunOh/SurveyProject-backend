package com.surveyplus.creator.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuestionLogic is a Querydsl query type for QuestionLogic
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionLogic extends EntityPathBase<QuestionLogic> {

    private static final long serialVersionUID = 1987777170L;

    public static final QQuestionLogic questionLogic = new QQuestionLogic("questionLogic");

    public final SimplePath<LogicCondition> conditionJson = createSimple("conditionJson", LogicCondition.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> sourceQuestionId = createNumber("sourceQuestionId", Long.class);

    public final NumberPath<Long> targetQuestionId = createNumber("targetQuestionId", Long.class);

    public final StringPath targetType = createString("targetType");

    public QQuestionLogic(String variable) {
        super(QuestionLogic.class, forVariable(variable));
    }

    public QQuestionLogic(Path<? extends QuestionLogic> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuestionLogic(PathMetadata metadata) {
        super(QuestionLogic.class, metadata);
    }

}


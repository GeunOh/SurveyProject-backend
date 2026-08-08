package com.surveyplus.creator.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurveyOption is a Querydsl query type for SurveyOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyOption extends EntityPathBase<SurveyOption> {

    private static final long serialVersionUID = 964889013L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSurveyOption surveyOption = new QSurveyOption("surveyOption");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final EnumPath<com.surveyplus.creator.survey.enums.SurveyOptionKey> key = createEnum("key", com.surveyplus.creator.survey.enums.SurveyOptionKey.class);

    public final QSurvey survey;

    public final StringPath value = createString("value");

    public QSurveyOption(String variable) {
        this(SurveyOption.class, forVariable(variable), INITS);
    }

    public QSurveyOption(Path<? extends SurveyOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSurveyOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSurveyOption(PathMetadata metadata, PathInits inits) {
        this(SurveyOption.class, metadata, inits);
    }

    public QSurveyOption(Class<? extends SurveyOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.survey = inits.isInitialized("survey") ? new QSurvey(forProperty("survey")) : null;
    }

}


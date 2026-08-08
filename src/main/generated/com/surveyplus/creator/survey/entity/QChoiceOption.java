package com.surveyplus.creator.survey.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChoiceOption is a Querydsl query type for ChoiceOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChoiceOption extends EntityPathBase<ChoiceOption> {

    private static final long serialVersionUID = -659116356L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChoiceOption choiceOption = new QChoiceOption("choiceOption");

    public final QChoice choice;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath key = createString("key");

    public final StringPath value = createString("value");

    public QChoiceOption(String variable) {
        this(ChoiceOption.class, forVariable(variable), INITS);
    }

    public QChoiceOption(Path<? extends ChoiceOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChoiceOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChoiceOption(PathMetadata metadata, PathInits inits) {
        this(ChoiceOption.class, metadata, inits);
    }

    public QChoiceOption(Class<? extends ChoiceOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.choice = inits.isInitialized("choice") ? new QChoice(forProperty("choice"), inits.get("choice")) : null;
    }

}


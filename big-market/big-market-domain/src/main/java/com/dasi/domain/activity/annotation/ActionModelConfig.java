package com.dasi.domain.activity.annotation;

import com.dasi.domain.activity.model.type.ActionModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionModelConfig {
    ActionModel actionModel();
}

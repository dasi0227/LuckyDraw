package com.dasi.domain.award.annotation;

import com.dasi.domain.award.model.type.AwardType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AwardTypeConfig {
    AwardType awardType();
}

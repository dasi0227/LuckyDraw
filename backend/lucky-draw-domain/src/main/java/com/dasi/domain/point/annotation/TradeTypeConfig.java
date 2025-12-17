package com.dasi.domain.point.annotation;

import com.dasi.domain.point.model.type.TradeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TradeTypeConfig {
    TradeType tradeType();
}

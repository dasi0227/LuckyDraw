package com.dasi.domain.strategy.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleCheckType {

    EQUAL(1, "等于"),
    GT(2, "大于"),
    LT(3, "小于"),
    GE(4, "大于等于"),
    LE(5, "小于等于"),
    ;


    private final Integer code;
    private final String type;

}

package com.dasi.domain.strategy.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleCheckType {
    EQUAL, // 等于
    GT, // 大于
    LT, // 小于
    GE, // 大于等于
    LE // 小于等于
}

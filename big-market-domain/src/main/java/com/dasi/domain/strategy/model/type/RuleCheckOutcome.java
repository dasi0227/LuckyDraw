package com.dasi.domain.strategy.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleCheckOutcome {
    PERMIT,   // 放行
    CAPTURE   // 捕获
}

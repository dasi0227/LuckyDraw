package com.dasi.domain.strategy.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleModel {

    RULE_LUCK,
    RULE_BLACKLIST,
    RULE_LOCK,
    RULE_FALLBACK,
    RULE_DEFAULT,
    RULE_STOCK;

}

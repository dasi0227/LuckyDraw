package com.dasi.domain.strategy.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleModel {

    RULE_WEIGHT("rule_weight"),
    RULE_BLACKLIST("rule_blacklist"),
    RULE_LOCK("rule_lock"),
    RULE_LUCK("rule_luck"),
    RULE_DEFAULT("rule_default"),
    RULE_STOCK("rule_stock"),
    ;

    private final String code;

}

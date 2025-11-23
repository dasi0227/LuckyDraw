package com.dasi.domain.strategy.model.check;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleCheckModel {

    RULE_WEIGHT("rule_weight","【抽奖前规则】根据抽奖权重，返回可抽奖范围", "before"),
    RULE_BLACKLIST("rule_blacklist","【抽奖前规则】根据黑名单，过滤用户", "before"),
    RULE_LOCK("rule_lock","【抽奖中规则】根据抽奖次数，解锁抽奖奖品", "during"),
    RULE_LUCK("rule_luck","【抽奖后规则】幸运奖都低", "after"),
    RULE_DEFAULT("rule_default","【默认规则】直接执行抽奖", "default"),
    RULE_STOCK("rule_stock","【默认规则】直接执行抽奖", "stock"),
    ;

    private final String name;
    private final String info;
    private final String type;

}

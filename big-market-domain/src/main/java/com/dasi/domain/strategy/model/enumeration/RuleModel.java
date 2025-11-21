package com.dasi.domain.strategy.model.enumeration;

import com.dasi.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum RuleModel {

    RULE_WEIGHT("rule_weight","【抽奖前规则】根据抽奖权重，返回可抽奖范围", "before"),
    RULE_BLACKLIST("rule_blacklist","【抽奖前规则】根据黑名单，过滤用户", "before"),
    RULE_LOCK("rule_lock","【抽奖中规则】根据抽奖次数，解锁抽奖奖品", "during"),
    RULE_LUCK("rule_luck","【抽奖后规则】幸运奖都低", "after"),
    RULE_DEFAULT("rule_default","【默认规则】直接执行抽奖", "default"),
    ;

    private final String name;
    private final String info;
    private final String type;

    public static boolean isType(String ruleModel, String type) {
        return type.equals(RuleModel.valueOf(ruleModel.toUpperCase()).type);
    }

    public String[] splitRuleModels(String ruleModels, String type) {
        if (ruleModels == null || ruleModels.trim().isEmpty()) {
            return new String[0];
        }

        List<String> ruleModelList = new ArrayList<>();

        for (String ruleModel : ruleModels.split(Constants.COMMA)) {
            if (RuleModel.isType(ruleModel, type)) {
                ruleModelList.add(ruleModel);
            }
        }

        return ruleModelList.toArray(new String[0]);
    }

}

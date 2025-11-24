package com.dasi.domain.strategy.model.entity;

import com.dasi.domain.strategy.model.rule.RuleModel;
import com.dasi.types.constant.Character;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyEntity {

    /** 抽奖策略ID */
    private Long strategyId;

    /** 抽奖策略描述 */
    private String strategyDesc;

    /** 抽奖规则模型 */
    private String ruleModels;

    public String[] splitRuleModels() {
        if (StringUtils.isBlank(ruleModels)) {
            return null;
        } else {
            return ruleModels.split(Character.COMMA);
        }
    }

    public boolean hasRuleWeight() {
        String[] ruleModels = splitRuleModels();
        if (ruleModels == null) {
            return false;
        }
        for (String ruleModel : ruleModels) {
            if (RuleModel.RULE_WEIGHT.name().equals(ruleModel)) {
                return true;
            }
        }
        return false;
    }

}

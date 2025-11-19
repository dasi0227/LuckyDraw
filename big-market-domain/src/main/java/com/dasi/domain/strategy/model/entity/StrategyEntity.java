package com.dasi.domain.strategy.model.entity;

import com.dasi.types.common.Constants;
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

    public String[] ruleModels() {
        if (StringUtils.isBlank(ruleModels)) {
            return null;
        } else {
            return ruleModels.split(Constants.COMMA);
        }
    }

    public boolean hasRuleWeight() {
        String[] ruleModels = this.ruleModels();
        if (ruleModels == null) {
            return false;
        }
        for (String ruleModel : ruleModels) {
            if (Constants.RuleModel.RULE_WEIGHT.equals(ruleModel)) {
                return true;
            }
        }
        return false;
    }

}

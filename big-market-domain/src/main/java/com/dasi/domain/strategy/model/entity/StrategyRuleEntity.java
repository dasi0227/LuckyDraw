package com.dasi.domain.strategy.model.entity;

import com.dasi.domain.strategy.model.type.RuleModel;
import com.dasi.types.constant.Delimiter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleEntity {

    /** 抽奖策略ID */
    private Long strategyId;

    /** 规则模型 */
    private String ruleModel;

    /** 规则比值 */
    private String ruleValue;

    /** 规则描述 */
    private String ruleDesc;

    public Map<String, List<Integer>> getRuleWeightValue() {
        if (!RuleModel.RULE_WEIGHT.getCode().equals(ruleModel)) return null;
        if (ruleValue == null || ruleValue.trim().isEmpty()) return null;

        Map<String, List<Integer>> ruleWeightValue = new HashMap<>();

        // 分割空格：得到不同【积分-奖品】组
        String[] groups = ruleValue.trim().split(Delimiter.SPACE);
        for (String group : groups) {
            // 分割冒号：得到【左侧积分】和【右侧奖品列表】
            String[] parts = group.split(Delimiter.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("权重规则格式非法：" + group);

            // 分割积分值和规则值
            String weight = parts[0];
            List<Integer> value = Arrays.stream(parts[1].trim().split(Delimiter.COMMA))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            // 放入 Map
            ruleWeightValue.put(weight, value);
        }

        return ruleWeightValue;
    }

}

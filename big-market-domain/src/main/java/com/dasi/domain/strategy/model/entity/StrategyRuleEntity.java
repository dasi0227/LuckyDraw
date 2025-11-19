package com.dasi.domain.strategy.model.entity;

import com.dasi.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyRuleEntity {

    /** 抽奖策略ID */
    private Long strategyId;

    /** 奖品ID（规则类型为策略级时可为空） */
    private Integer awardId;

    /** 规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;

    /** 规则模型 */
    private String ruleModel;

    /** 规则比值 */
    private String ruleValue;

    /** 规则描述 */
    private String ruleDesc;

    public Map<String, List<Integer>> getRuleWeightValue() {
        if (!Constants.RuleModel.RULE_WEIGHT.equals(ruleModel)) return null;
        if (ruleValue == null || ruleValue.trim().isEmpty()) return null;

        Map<String, List<Integer>> ruleWeightValue = new HashMap<>();

        // 分割空格：得到不同【积分-奖品】组
        String[] groups = ruleValue.trim().split(Constants.BLANK);
        for (String group : groups) {
            // 分割冒号：得到【左侧积分】和【右侧奖品列表】
            String[] parts = group.split(Constants.COLON);
            if (parts.length != 2) throw new IllegalArgumentException("rule_weight invalid" + group);

            // 分割积分值和规则值
            String weight = parts[0];
            List<Integer> value = Arrays.stream(parts[1].trim().split(Constants.COMMA))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            // 放入 Map
            ruleWeightValue.put(weight, value);
        }

        return ruleWeightValue;
    }

}

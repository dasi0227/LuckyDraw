package com.dasi.domain.strategy.model.dto;

import com.dasi.domain.strategy.model.rule.RuleCheckOutcome;
import com.dasi.domain.strategy.model.rule.RuleModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCheckResult {

    /** 奖品 ID */
    private Integer awardId;

    /** 最后应用的规则模型 */
    private RuleModel ruleModel;

    /** 最后应用的检查结果 */
    private RuleCheckOutcome ruleCheckOutcome;

}

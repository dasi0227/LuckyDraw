package com.dasi.domain.strategy.model.check;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCheckResponse {

    /** 奖品 ID */
    private Integer awardId;

    /** 最后应用的规则模型 */
    private RuleCheckModel ruleCheckModel;

    /** 最后应用的检查结果 */
    private RuleCheckResult ruleCheckResult;

}

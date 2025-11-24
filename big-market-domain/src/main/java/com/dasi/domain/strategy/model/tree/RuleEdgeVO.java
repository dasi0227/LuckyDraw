package com.dasi.domain.strategy.model.tree;

import com.dasi.domain.strategy.model.rule.RuleCheckOutcome;
import com.dasi.domain.strategy.model.rule.RuleCheckType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当且仅当传递的实际的 Result 和 RuleCheckOutcome 符合 ruleCheckType，才会走这条边，即从 from -> to
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEdgeVO {

    /** 规则树 ID */
    private String treeId;

    /** 规则连线头 */
    private String ruleNodeFrom;

    /** 规则连线尾 */
    private String ruleNodeTo;

    /** 规则连线类型 */
    private RuleCheckType ruleCheckType;

    /** 规则连线结果 */
    private RuleCheckOutcome ruleCheckOutcome;

}

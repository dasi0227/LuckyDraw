package com.dasi.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleNodeEntity {

    /** 规则树ID */
    private String treeId;

    /** 规则模型 */
    private String ruleModel;

    /** 规则描述 */
    private String ruleDesc;

    /** 规则值 */
    private String ruleValue;

}

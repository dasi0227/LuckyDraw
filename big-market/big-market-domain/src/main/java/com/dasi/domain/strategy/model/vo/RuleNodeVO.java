package com.dasi.domain.strategy.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleNodeVO {

    /** 规则树 ID */
    private String treeId;

    /** 规则模型 */
    private String ruleModel;

    /** 规则值 */
    private String ruleValue;

    /** 规则连线 */
    private List<RuleEdgeVO> ruleEdgeList;

}

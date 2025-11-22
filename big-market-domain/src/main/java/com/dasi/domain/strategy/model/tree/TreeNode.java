package com.dasi.domain.strategy.model.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeNode {

    /** 规则树 ID */
    private Integer treeId;

    /** 规则节点描述 */
    private String ruleDesc;

    /** 规则模型 */
    private String ruleModel;

    /** 规则值 */
    private String ruleValue;

    /** 规则连线 */
    private List<TreeEdge> treeLineList;

}

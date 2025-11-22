package com.dasi.domain.strategy.model.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeRoot {

    /** 规则树 ID */
    private Integer id;

    /** 规则树名称 */
    private String treeName;

    /** 规则树描述 */
    private String treeDesc;

    /** 规则树根 */
    private String treeRoot;

    /** 规则节点 */
    private Map<String, TreeNode> treeNodeMap;

}

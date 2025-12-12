package com.dasi.domain.strategy.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleTreeVO {

    /** 规则树 ID */
    private String treeId;

    /** 规则树根 */
    private String treeRoot;

    /** 规则节点 */
    private Map<String, RuleNodeVO> treeNodeMap;

}

package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleEdge {

    /** 自增主键 */
    private Long id;

    /** 规则树ID */
    private String treeId;

    /** 起点节点 */
    private String ruleNodeFrom;

    /** 终点节点 */
    private String ruleNodeTo;

    /** 检查类型 */
    private String ruleCheckType;

    /** 检查结果 */
    private String ruleCheckResult;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

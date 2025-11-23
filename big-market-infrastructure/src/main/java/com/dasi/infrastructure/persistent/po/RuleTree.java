package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleTree {

    /** 自增主键 */
    private Long id;

    /** 规则树业务ID */
    private String treeId;

    /** 规则树名称 */
    private String treeName;

    /** 规则树描述 */
    private String treeDesc;

    /** 根节点的规则模型 */
    private String treeRoot;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
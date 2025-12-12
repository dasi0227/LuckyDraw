package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleNode {

    /** 自增主键 */
    private Long id;

    /** 规则树ID */
    private String treeId;

    /** 规则模型 */
    private String ruleModel;

    /** 规则描述 */
    private String ruleDesc;

    /** 规则值 */
    private String ruleValue;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

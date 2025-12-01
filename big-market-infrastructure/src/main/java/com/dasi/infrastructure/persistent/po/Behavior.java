package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Behavior {

    /** 自增id */
    private Long id;

    /** 行为id */
    private Long behaviorId;

    /** 行为描述 */
    private String behaviorDesc;

    /** 行为类型 */
    private String behaviorType;

    /** 行为奖励 */
    private String behaviorReward;

    /** 行为奖励值 */
    private String behaviorConfig;

    /** 行为状态 */
    private String behaviorState;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

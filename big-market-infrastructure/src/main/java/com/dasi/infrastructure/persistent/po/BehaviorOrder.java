package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BehaviorOrder {

    /** 自增id */
    private Long id;

    /** 订单id */
    private String orderId;

    /** 业务id */
    private String bizId;

    /** 用户id */
    private String userId;

    /** 行为id */
    private Long behaviorId;

    /** 行为类型 */
    private String behaviorType;

    /** 行为奖励 */
    private String behaviorReward;

    /** 行为奖励值 */
    private String behaviorConfig;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

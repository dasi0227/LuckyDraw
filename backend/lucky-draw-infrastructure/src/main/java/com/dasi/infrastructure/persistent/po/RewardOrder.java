package com.dasi.infrastructure.persistent.po;

import com.dasi.domain.behavior.model.type.BehaviorType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RewardOrder {

    /** 自增id */
    private Long id;

    /** 订单id */
    private String orderId;

    /** 业务id */
    private String bizId;

    /** 用户id */
    private String userId;

    /** 用户id */
    private Long activityId;

    /** 行为类型 */
    private BehaviorType behaviorType;

    /** 奖励类型 */
    private String rewardType;

    /** 奖励值 */
    private String rewardValue;

    /** 奖励状态 */
    private String rewardState;

    /** 奖励描述 */
    private String rewardDesc;

    /** 奖励时间 */
    private LocalDateTime rewardTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

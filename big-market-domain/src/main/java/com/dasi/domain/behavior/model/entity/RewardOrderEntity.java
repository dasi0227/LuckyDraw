package com.dasi.domain.behavior.model.entity;

import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.model.type.RewardState;
import com.dasi.domain.behavior.model.type.RewardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardOrderEntity {

    /** 订单id */
    private String orderId;

    /** 业务id */
    private String bizId;

    /** 用户id */
    private String userId;

    /** 活动id */
    private Long activityId;

    /** 行为id */
    private BehaviorType behaviorType;

    /** 奖励类型 */
    private RewardType rewardType;

    /** 奖励值 */
    private String rewardValue;

    /** 奖励状态 */
    private RewardState rewardState;

    /** 奖励状态 */
    private String rewardDesc;

    /** 奖励时间 */
    private LocalDateTime rewardTime;

}

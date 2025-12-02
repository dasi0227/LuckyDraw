package com.dasi.domain.behavior.model.entity;

import com.dasi.domain.behavior.model.type.RewardState;
import com.dasi.domain.behavior.model.type.RewardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** 行为id */
    private Long behaviorId;

    /** 奖励类型 */
    private RewardType rewardType;

    /** 奖励值 */
    private String rewardValue;

    /** 奖励状态 */
    private RewardState rewardState;

}

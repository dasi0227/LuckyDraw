package com.dasi.domain.behavior.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorOrderEntity {

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

}

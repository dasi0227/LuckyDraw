package com.dasi.domain.behavior.model.entity;

import com.dasi.domain.behavior.model.type.BehaviorState;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.model.type.RewardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {

    /** 活动id */
    private Long activityId;

    /** 行为类型 */
    private BehaviorType behaviorType;

    /** 行为状态 */
    private BehaviorState behaviorState;

    /** 行为奖励 */
    private RewardType rewardType;

    /** 奖励描述 */
    private String rewardDesc;

    /** 行为奖励值 */
    private String rewardValue;

}

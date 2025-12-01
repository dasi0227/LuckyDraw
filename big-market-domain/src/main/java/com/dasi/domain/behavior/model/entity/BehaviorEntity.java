package com.dasi.domain.behavior.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {

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

}

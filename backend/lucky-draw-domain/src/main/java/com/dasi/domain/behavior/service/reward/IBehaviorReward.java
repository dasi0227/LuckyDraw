package com.dasi.domain.behavior.service.reward;

import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;

public interface IBehaviorReward {

    BehaviorResult doBehaviorReward(BehaviorContext behaviorContext);

    void updateRewardOrderState(RewardOrderEntity rewardOrderEntity);

}

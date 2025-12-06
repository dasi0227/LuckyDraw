package com.dasi.domain.behavior.repository;

import com.dasi.domain.behavior.model.aggregate.RewardOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.type.BehaviorType;

import java.util.List;

public interface IBehaviorRepository {

    void saveRewardOrder(String userId, List<RewardOrderAggregate> rewardOrderAggregateList);

    void updateRewardOrderState(RewardOrderEntity rewardOrderEntity);

    List<BehaviorEntity> queryBehaviorList(Long activityId, BehaviorType behaviorType);

    Boolean querySign(String userId, Long activityId);
}

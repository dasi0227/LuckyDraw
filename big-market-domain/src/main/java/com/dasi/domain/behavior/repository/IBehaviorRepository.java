package com.dasi.domain.behavior.repository;

import com.dasi.domain.behavior.model.aggregate.RewardOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;

import java.util.List;

public interface IBehaviorRepository {

    List<BehaviorEntity> queryBehaviorListByBehaviorIds(List<Long> behaviorIds);

    void saveRewardOrder(String userId, List<RewardOrderAggregate> rewardOrderAggregateList);

    int updateRewardOrderState(RewardOrderEntity rewardOrderEntity);
}

package com.dasi.domain.behavior.repository;

import com.dasi.domain.behavior.model.aggregate.BehaviorOrderAggregate;
import com.dasi.domain.behavior.model.entity.BehaviorEntity;

import java.util.List;

public interface IBehaviorRepository {

    List<BehaviorEntity> queryBehaviorListByBehaviorIds(List<Long> behaviorIds);

    void saveBehaviorOrder(String userId, List<BehaviorOrderAggregate> behaviorOrderAggregateList);
}

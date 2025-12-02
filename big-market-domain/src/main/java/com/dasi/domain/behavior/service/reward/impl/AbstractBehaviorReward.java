package com.dasi.domain.behavior.service.reward.impl;

import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBehaviorReward implements IBehaviorReward {

    protected final IBehaviorRepository behaviorRepository;

    public AbstractBehaviorReward(IBehaviorRepository behaviorRepository) {
        this.behaviorRepository = behaviorRepository;
    }

    @Override
    public BehaviorResult doBehaviorReward(BehaviorContext behaviorContext) {

        // 1. 参数校验
        String userId = behaviorContext.getUserId();
        String businessNo = behaviorContext.getBusinessNo();
        List<Long> behaviorIds = behaviorContext.getBehaviorIds();
        if (StringUtils.isBlank(businessNo) || StringUtils.isBlank(userId) || behaviorIds == null || behaviorIds.isEmpty()) {
            throw new AppException("【奖励】参数不正确");
        }

        // 2. 保存订单
        List<RewardOrderEntity> rewardOrderEntityList = saveRewardOrder(userId, businessNo, behaviorIds);
        if (rewardOrderEntityList == null) {
            throw new AppException("【奖励】动作触发奖励失败");
        }

        // 3. 返回订单信息
        List<String> orderIds = rewardOrderEntityList.stream()
                .map(RewardOrderEntity::getOrderId)
                .collect(Collectors.toList());
        return BehaviorResult.builder().orderIds(orderIds).build();
    }

    protected abstract List<RewardOrderEntity> saveRewardOrder(String userId, String businessNo, List<Long> behaviorIds);

}

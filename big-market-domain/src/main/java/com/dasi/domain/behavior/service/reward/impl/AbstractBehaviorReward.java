package com.dasi.domain.behavior.service.reward.impl;

import com.dasi.domain.behavior.model.entity.BehaviorEntity;
import com.dasi.domain.behavior.model.entity.RewardOrderEntity;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.model.type.BehaviorType;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.domain.behavior.service.reward.IBehaviorReward;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractBehaviorReward implements IBehaviorReward {

    protected final IBehaviorRepository behaviorRepository;

    public AbstractBehaviorReward(IBehaviorRepository behaviorRepository) {
        this.behaviorRepository = behaviorRepository;
    }

    @Override
    public BehaviorResult doBehaviorReward(BehaviorContext behaviorContext) {

        // 1. 参数校验
        String userId = behaviorContext.getUserId();
        Long activityId = behaviorContext.getActivityId();
        BehaviorType behaviorType = behaviorContext.getBehaviorType();
        String businessNo = behaviorContext.getBusinessNo();
        if (StringUtils.isBlank(businessNo)) throw new AppException("（返利）缺少参数 businessNo");
        if (StringUtils.isBlank(userId)) throw new AppException("（返利）缺少参数 userId");
        if (activityId == null) throw new AppException("（返利）缺少参数 activityId");
        if (behaviorType == null) throw new AppException("（返利）缺少参数 behaviorType");

        // 2. 查询行为奖励
        List<BehaviorEntity> behaviorEntityList = queryBehaviorList(activityId, behaviorType);

        // 3. 保存订单
        List<RewardOrderEntity> rewardOrderEntityList = saveRewardOrder(activityId, userId, businessNo, behaviorEntityList);
        if (rewardOrderEntityList == null) {
            throw new AppException("（返利）行为触发奖励失败");
        }

        // 4. 返回订单信息
        List<String> rewardDescList = rewardOrderEntityList.stream()
                .map(RewardOrderEntity::getRewardDesc)
                .collect(Collectors.toList());
        List<String> orderIds = rewardOrderEntityList.stream()
                .map(RewardOrderEntity::getOrderId)
                .collect(Collectors.toList());

        return BehaviorResult.builder()
                .orderIds(orderIds)
                .rewardDescList(rewardDescList)
                .build();
    }

    protected abstract List<BehaviorEntity> queryBehaviorList(Long activityId, BehaviorType behaviorType);

    protected abstract List<RewardOrderEntity> saveRewardOrder(Long activityId, String userId, String businessNo, List<BehaviorEntity> behaviorEntityList);

}

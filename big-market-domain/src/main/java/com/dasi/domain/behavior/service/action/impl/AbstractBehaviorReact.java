package com.dasi.domain.behavior.service.action.impl;

import com.dasi.domain.behavior.model.entity.BehaviorOrderEntity;
import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.repository.IBehaviorRepository;
import com.dasi.domain.behavior.service.action.IBehaviorReact;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBehaviorReact implements IBehaviorReact {

    protected final IBehaviorRepository behaviorRepository;

    public AbstractBehaviorReact(IBehaviorRepository behaviorRepository) {
        this.behaviorRepository = behaviorRepository;
    }

    @Override
    public BehaviorResult doBehaviorReact(BehaviorContext behaviorContext) {

        // 1. 参数校验
        String userId = behaviorContext.getUserId();
        String businessNo = behaviorContext.getBusinessNo();
        List<Long> behaviorIds = behaviorContext.getBehaviorIds();
        if (StringUtils.isBlank(businessNo) || StringUtils.isBlank(userId) || behaviorIds == null || behaviorIds.isEmpty()) {
            throw new AppException("【用户动作】参数不正确");
        }

        // 2. 保存订单
        List<BehaviorOrderEntity> behaviorOrderEntityList = saveBehaviorOrder(userId, businessNo, behaviorIds);
        if (behaviorOrderEntityList == null) {
            throw new AppException("【用户动作】动作触发奖励失败");
        }

        // 3. 返回订单信息
        List<String> orderIds = behaviorOrderEntityList.stream()
                .map(BehaviorOrderEntity::getOrderId)
                .collect(Collectors.toList());
        return BehaviorResult.builder().orderIds(orderIds).build();
    }

    protected abstract List<BehaviorOrderEntity> saveBehaviorOrder(String userId, String businessNo, List<Long> behaviorIds);

}

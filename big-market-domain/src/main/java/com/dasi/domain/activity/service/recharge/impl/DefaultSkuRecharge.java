package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeOrderEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.io.SkuRechargeContext;
import com.dasi.domain.activity.model.type.RechargeState;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.chain.ActivityChainFactory;
import com.dasi.domain.activity.service.chain.IActivityChain;
import com.dasi.domain.common.IUniqueIdGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DefaultSkuRecharge extends AbstractSkuRecharge {

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Resource
    private ActivityChainFactory activityChainFactory;

    public DefaultSkuRecharge(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    public Boolean checkRechargeAvailable(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity) {
        ActionChainCheckAggregate actionChainCheckAggregate = ActionChainCheckAggregate.builder()
                .activityEntity(activityEntity)
                .activitySkuEntity(activitySkuEntity)
                .build();
        IActivityChain activityChain = activityChainFactory.getRechargeActionChain();
        return activityChain.action(actionChainCheckAggregate);
    }

    @Override
    protected RechargeOrderEntity saveRechargeOrder(SkuRechargeContext skuRechargeContext, ActivitySkuEntity activitySkuEntity) {

        // 1. 构建订单
        RechargeOrderEntity rechargeOrderEntity = RechargeOrderEntity.builder()
                .orderId(uniqueIdGenerator.nextRechargeOrderId())
                .bizId(skuRechargeContext.getBizId())
                .activityId(activitySkuEntity.getActivityId())
                .userId(skuRechargeContext.getUserId())
                .skuId(skuRechargeContext.getSkuId())
                .count(activitySkuEntity.getCount())
                .rechargeState(RechargeState.CREATED)
                .rechargeTime(LocalDateTime.now())
                .build();

        // 2. 充值到账
        activityRepository.saveRechargeOrder(rechargeOrderEntity);
        return rechargeOrderEntity;
    }

}

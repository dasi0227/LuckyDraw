package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.model.entity.ActivityQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.type.ActivityOrderState;
import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.action.chain.ActionChainFactory;
import com.dasi.domain.activity.service.action.chain.IActionChain;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DefaultActivityRecharge extends AbstractActivityRecharge {

    @Resource
    private ActionChainFactory actionChainFactory;

    public DefaultActivityRecharge(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    public Boolean checkRechargeValid(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityQuotaEntity activityQuotaEntity) {
        IActionChain actionChain = actionChainFactory.getRechargeActionChain();
        return actionChain.action(activitySkuEntity, activityEntity, activityQuotaEntity);
    }

    @Override
    protected RechargeResult createRechargeOrder(RechargeContext rechargeContext, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityQuotaEntity activityQuotaEntity) {

        // 1. 构建订单
        ActivityOrderEntity activityOrderEntity = ActivityOrderEntity.builder()
                .orderId(RandomStringUtils.randomNumeric(12))
                .bizId(rechargeContext.getBizId())
                .userId(rechargeContext.getUserId())
                .skuId(rechargeContext.getSkuId())
                .activityId(activityEntity.getActivityId())
                .activityQuotaId(activityQuotaEntity.getActivityQuotaId())
                .strategyId(activityEntity.getStrategyId())
                .totalCount(activityQuotaEntity.getTotalCount())
                .monthCount(activityQuotaEntity.getMonthCount())
                .dayCount(activityQuotaEntity.getDayCount())
                .orderTime(LocalDateTime.now())
                .activityOrderState(ActivityOrderState.COMPLETED.getCode())
                .build();

        // 2. 充值到账
        activityRepository.saveActivitySkuOrder(activityOrderEntity);

        // 3. 构造结果
        return RechargeResult.builder()
                .userId(activityOrderEntity.getUserId())
                .orderId(activityOrderEntity.getOrderId())
                .totalCount(activityOrderEntity.getTotalCount())
                .monthCount(activityOrderEntity.getMonthCount())
                .dayCount(activityOrderEntity.getDayCount())
                .build();
    }

}

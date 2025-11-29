package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.model.entity.RechargeQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeOrderEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
import com.dasi.domain.activity.model.type.RechargeState;
import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.action.chain.ActionChainFactory;
import com.dasi.domain.activity.service.action.chain.IActionChain;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DefaultSkuRecharge extends AbstractSkuRecharge {

    @Resource
    private ActionChainFactory actionChainFactory;

    public DefaultSkuRecharge(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    public Boolean checkRechargeAvailable(RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity) {
        ActionChainCheckAggregate actionChainCheckAggregate = ActionChainCheckAggregate.builder()
                .activityEntity(activityEntity)
                .rechargeSkuEntity(rechargeSkuEntity)
                .rechargeQuotaEntity(rechargeQuotaEntity)
                .build();
        IActionChain actionChain = actionChainFactory.getRechargeActionChain();
        return actionChain.action(actionChainCheckAggregate);
    }

    @Override
    protected RechargeResult createRechargeOrder(RechargeContext rechargeContext, RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity) {

        // 1. 构建订单
        RechargeOrderEntity rechargeOrderEntity = RechargeOrderEntity.builder()
                .orderId(RandomStringUtils.randomNumeric(12))
                .bizId(rechargeContext.getBizId())
                .userId(rechargeContext.getUserId())
                .skuId(rechargeContext.getSkuId())
                .activityId(activityEntity.getActivityId())
                .quotaId(rechargeQuotaEntity.getQuotaId())
                .strategyId(activityEntity.getStrategyId())
                .totalCount(rechargeQuotaEntity.getTotalCount())
                .monthCount(rechargeQuotaEntity.getMonthCount())
                .dayCount(rechargeQuotaEntity.getDayCount())
                .rechargeTime(LocalDateTime.now())
                .rechargeState(RechargeState.COMPLETED.getCode())
                .build();

        // 2. 充值到账
        activityRepository.saveRechargeOrder(rechargeOrderEntity);

        // 3. 构造结果
        return RechargeResult.builder()
                .orderId(rechargeOrderEntity.getOrderId())
                .totalCount(rechargeOrderEntity.getTotalCount())
                .monthCount(rechargeOrderEntity.getMonthCount())
                .dayCount(rechargeOrderEntity.getDayCount())
                .build();

    }

}

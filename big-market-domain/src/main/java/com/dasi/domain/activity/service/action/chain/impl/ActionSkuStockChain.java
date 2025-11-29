package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.RechargeSkuStockEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.stock.IActivityStock;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component(ActionModel.SKU_STOCK)
public class ActionSkuStockChain extends AbstractActionChain {

    @Resource
    private IActivityStock activityStock;

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {
        ActivityEntity activityEntity = actionChainCheckAggregate.getActivityEntity();
        RechargeSkuEntity rechargeSkuEntity = actionChainCheckAggregate.getRechargeSkuEntity();

        if (rechargeSkuEntity.getStockSurplus() <= 0) {
            log.info("【活动责任链 - action_stock】库存为空：activityId = {}, skuId = {}", activityEntity.getActivityId(), rechargeSkuEntity.getSkuId());
            return false;
        }

        Long surplus = activityStock.subtractRechargeSkuStock(rechargeSkuEntity.getSkuId(), activityEntity.getActivityEndTime());
        if (surplus == -1L) {
            log.info("【活动责任链 - action_stock】库存为空：activityId = {}, skuId = {}", activityEntity.getActivityId(), rechargeSkuEntity.getSkuId());
            return false;
        }
        if (surplus == -2L)  throw new AppException("扣减库存失败：" + rechargeSkuEntity);
        RechargeSkuStockEntity rechargeSkuStockEntity = RechargeSkuStockEntity.builder()
                .skuId(rechargeSkuEntity.getSkuId())
                .activityId(activityEntity.getActivityId())
                .build();
        activityRepository.sendRechargeSkuStockConsumeToMQ(rechargeSkuStockEntity);
        log.info("【活动责任链 - action_stock】扣减库存：activityId = {}，skuId = {}, surplus = {}->{}", activityEntity.getActivityId(), rechargeSkuEntity.getSkuId(), surplus + 1, surplus);

        return next().action(actionChainCheckAggregate);
    }
}

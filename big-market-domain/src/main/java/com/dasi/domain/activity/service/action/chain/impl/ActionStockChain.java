package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.dto.SkuStock;
import com.dasi.domain.activity.model.entity.RechargeQuotaEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.stock.IActivityStock;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component(ActionModel.ACTION_STOCK)
public class ActionStockChain extends AbstractActionChain {

    @Resource
    private IActivityStock activityStock;

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public Boolean action(RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity) {
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
        SkuStock skuStock = SkuStock.builder()
                .skuId(rechargeSkuEntity.getSkuId())
                .activityId(activityEntity.getActivityId())
                .build();
        activityRepository.sendRechargeSkuStockConsumeToMQ(skuStock);
        log.info("【活动责任链 - action_stock】扣减库存：activityId = {}，skuId = {}, surplus = {}->{}", activityEntity.getActivityId(), rechargeSkuEntity.getSkuId(), surplus + 1, surplus);

        return next().action(rechargeSkuEntity, activityEntity, rechargeQuotaEntity);
    }
}

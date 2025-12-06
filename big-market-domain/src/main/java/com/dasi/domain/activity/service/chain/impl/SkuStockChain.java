package com.dasi.domain.activity.service.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.io.ActivitySkuStock;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.stock.IActivityStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component(ActionModel.SKU_STOCK)
public class SkuStockChain extends AbstractActivityChain {

    @Resource
    private IActivityStock activityStock;

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {
        ActivityEntity activityEntity = actionChainCheckAggregate.getActivityEntity();
        ActivitySkuEntity activitySkuEntity = actionChainCheckAggregate.getActivitySkuEntity();

        if (activitySkuEntity.getStockSurplus() <= 0) {
            log.info("【检查】sku_stock 拦截（库存为空）：activityId={}, skuId={}", activityEntity.getActivityId(), activitySkuEntity.getSkuId());
            return false;
        }

        Long surplus = activityStock.subtractActivitySkuStock(activitySkuEntity.getSkuId(), activityEntity.getActivityEndTime());
        if (surplus == -1L) {
            log.info("【检查】sku_stock 拦截（库存为空）：activityId={}, skuId={}", activityEntity.getActivityId(), activitySkuEntity.getSkuId());
            return false;
        }
        if (surplus == -2L) {
            log.info("【检查】sku_stock 拦截（扣减失败）：activityId={}, skuId={}", activityEntity.getActivityId(), activitySkuEntity.getSkuId());
            return false;
        }
        ActivitySkuStock activitySkuStock = ActivitySkuStock.builder()
                .skuId(activitySkuEntity.getSkuId())
                .activityId(activityEntity.getActivityId())
                .build();
        activityRepository.sendActivitySkuStockConsumeToMQ(activitySkuStock);
        log.info("【检查】SKU_STOCK 放行：activityId={}，skuId={}, surplus={}->{}", activityEntity.getActivityId(), activitySkuEntity.getSkuId(), surplus + 1, surplus);

        return next().action(actionChainCheckAggregate);
    }
}

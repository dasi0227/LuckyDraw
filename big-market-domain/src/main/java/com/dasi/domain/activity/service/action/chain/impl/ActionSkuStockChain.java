package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.dto.ActivitySkuStock;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.action.chain.AbstractActionChain;
import com.dasi.domain.activity.service.stock.IActivityStock;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component(ActionModel.ACTION_SKU_STOCK)
public class ActionSkuStockChain extends AbstractActionChain {

    @Resource
    private IActivityStock activityStock;

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public void action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        Long surplus = activityStock.subtractActivitySkuStock(activitySkuEntity.getSku(), activityEntity.getEndTime());
        if (surplus == -1L)  throw new AppException("扣减库存失败：" + activitySkuEntity);
        ActivitySkuStock activitySkuStock = ActivitySkuStock.builder()
                .sku(activitySkuEntity.getSku())
                .activityId(activityEntity.getActivityId())
                .build();
        activityRepository.sendActivitySkuStockConsumeToMQ(activitySkuStock);
        log.info("【活动责任链 - action_sku_stock】扣减库存：sku = {}, surplus = {}->{}", activitySkuEntity.getSku(), surplus + 1, surplus);
    }
}

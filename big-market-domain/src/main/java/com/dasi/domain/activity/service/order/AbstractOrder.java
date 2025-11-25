package com.dasi.domain.activity.service.order;

import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.repository.IActivityRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractOrder implements IOrder {

    protected IActivityRepository activityRepository;

    public AbstractOrder(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public ActivityOrderEntity createActivityOrder(ActivityShoppingCartEntity activityShoppingCartEntity) {
        // 1. 通过 sku 查询活动信息
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuBySku(activityShoppingCartEntity.getSku());

        // 2. 查询活动信息
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activitySkuEntity.getActivityId());

        // 3. 查询次数信息
        ActivityCountEntity activityCountEntity = activityRepository.queryActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        log.info("【创建订单】activitySkuEntity = {}", activitySkuEntity);
        log.info("【创建订单】activityEntity = {}", activityEntity);
        log.info("【创建订单】activityCountEntity = {}", activityCountEntity);

        return ActivityOrderEntity.builder().build();
    }
}

package com.dasi.domain.activity.repository;

import com.dasi.domain.activity.model.dto.ActivitySkuStock;
import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.dto.SkuOrder;

import java.time.LocalDateTime;

public interface IActivityRepository {

    ActivitySkuEntity queryActivitySkuBySku(Long sku);

    ActivityEntity queryActivityByActivityId(Long activityId);

    ActivityCountEntity queryActivityCountByActivityCountId(Long activityCountId);

    void saveOrder(SkuOrder skuOrder);

    void cacheActivitySkuStockSurplus(Long sku, Integer stockSurplus);

    Long subtractActivitySkuStock(Long sku, LocalDateTime endDatetime);

    void sendActivitySkuStockConsumeToMQ(ActivitySkuStock activitySkuStock);

    ActivitySkuStock getQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);
}

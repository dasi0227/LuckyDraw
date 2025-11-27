package com.dasi.domain.activity.repository;

import com.dasi.domain.activity.model.dto.ActivitySkuStock;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import com.dasi.domain.activity.model.entity.ActivityQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;

import java.time.LocalDateTime;

public interface IActivityRepository {

    ActivitySkuEntity queryActivitySkuBySkuId(Long skuId);

    ActivityEntity queryActivityByActivityId(Long activityId);

    ActivityQuotaEntity queryActivityQuotaByActivityQuotaId(Long activityCountId);

    void saveActivitySkuOrder(ActivityOrderEntity activityOrderEntity);

    void cacheActivitySkuStockSurplus(Long skuId, Integer stockSurplus);

    Long subtractActivitySkuStockSurplus(Long skuId, LocalDateTime endTime);

    void sendActivitySkuStockConsumeToMQ(ActivitySkuStock activitySkuStock);

    ActivitySkuStock getQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long skuId);

    void clearActivitySkuStock(Long skuId);
}

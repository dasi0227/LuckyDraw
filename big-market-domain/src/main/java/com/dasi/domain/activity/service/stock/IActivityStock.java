package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.queue.ActivitySkuStock;

import java.time.LocalDateTime;

public interface IActivityStock {

    Long subtractActivitySkuStock(Long skuId, LocalDateTime activityEndTime);

    ActivitySkuStock getQueueValue();

    void clearQueueValue();

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

}

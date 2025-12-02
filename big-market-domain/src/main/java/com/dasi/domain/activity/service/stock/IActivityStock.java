package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.queue.RechargeSkuStock;

import java.time.LocalDateTime;

public interface IActivityStock {

    Long subtractRechargeSkuStock(Long skuId, LocalDateTime activityEndTime);

    RechargeSkuStock getQueueValue();

    void clearQueueValue();

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

}

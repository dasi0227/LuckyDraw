package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.entity.RechargeSkuStockEntity;

import java.time.LocalDateTime;

public interface IActivityStock {

    Long subtractRechargeSkuStock(Long skuId, LocalDateTime endDatetime);

    RechargeSkuStockEntity getQueueValue() throws InterruptedException;

    void clearQueueValue();

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

}

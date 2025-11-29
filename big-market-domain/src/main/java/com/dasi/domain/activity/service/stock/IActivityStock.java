package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.dto.RechargeSkuStock;

import java.time.LocalDateTime;

public interface IActivityStock {

    boolean assembleRechargeSkuStock(Long sku);

    Long subtractRechargeSkuStock(Long sku, LocalDateTime endDatetime);

    RechargeSkuStock getQueueValue() throws InterruptedException;

    void clearQueueValue();

    void updateRechargeSkuStock(Long sku);

    void clearRechargeSkuStock(Long sku);

}

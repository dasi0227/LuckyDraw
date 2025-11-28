package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.dto.SkuStock;

import java.time.LocalDateTime;

public interface IActivityStock {

    boolean assembleRechargeSkuStock(Long sku);

    Long subtractRechargeSkuStock(Long sku, LocalDateTime endDatetime);

    SkuStock getQueueValue() throws InterruptedException;

    void clearQueueValue();

    void updateRechargeSkuStock(Long sku);

    void clearRechargeSkuStock(Long sku);

}

package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.dto.ActivitySkuStock;

import java.time.LocalDateTime;

public interface IActivityStock {

    boolean assembleActivitySkuStock(Long sku);

    Long subtractActivitySkuStock(Long sku, LocalDateTime endDatetime);

    ActivitySkuStock getQueueValue() throws InterruptedException;

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

}

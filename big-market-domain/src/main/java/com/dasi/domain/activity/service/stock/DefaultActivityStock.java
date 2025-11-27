package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.dto.ActivitySkuStock;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class DefaultActivityStock implements IActivityStock {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public boolean assembleActivitySkuStock(Long sku) {
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuBySkuId(sku);
        activityRepository.cacheActivitySkuStockSurplus(sku, activitySkuEntity.getStockSurplus());
        log.info("【活动装配】sku = {}, activitySkuStockSurplus = {}", sku, activitySkuEntity.getStockSurplus());
        return false;
    }

    @Override
    public Long subtractActivitySkuStock(Long sku, LocalDateTime endTime) {
        return activityRepository.subtractActivitySkuStockSurplus(sku, endTime);
    }

    @Override
    public ActivitySkuStock getQueueValue() {
        return activityRepository.getQueueValue();
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        activityRepository.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activityRepository.clearActivitySkuStock(sku);
    }

}

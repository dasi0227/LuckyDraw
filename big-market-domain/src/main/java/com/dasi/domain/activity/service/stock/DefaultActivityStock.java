package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.dto.SkuStock;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
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
    public boolean assembleRechargeSkuStock(Long sku) {
        RechargeSkuEntity rechargeSkuEntity = activityRepository.queryRechargeSkuBySkuId(sku);
        activityRepository.cacheRechargeSkuStockSurplus(sku, rechargeSkuEntity.getStockSurplus());
        log.info("【活动装配】sku = {}, rechargeSkuStockSurplus = {}", sku, rechargeSkuEntity.getStockSurplus());
        return false;
    }

    @Override
    public Long subtractRechargeSkuStock(Long sku, LocalDateTime endTime) {
        return activityRepository.subtractRechargeSkuStockSurplus(sku, endTime);
    }

    @Override
    public SkuStock getQueueValue() {
        return activityRepository.getQueueValue();
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void updateRechargeSkuStock(Long sku) {
        activityRepository.updateRechargeSkuStock(sku);
    }

    @Override
    public void clearRechargeSkuStock(Long sku) {
        activityRepository.clearRechargeSkuStock(sku);
    }

}

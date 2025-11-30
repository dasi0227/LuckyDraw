package com.dasi.domain.activity.service.stock;

import com.dasi.domain.activity.model.entity.RechargeSkuStockEntity;
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
    public Long subtractRechargeSkuStock(Long skuId, LocalDateTime activityEndTime) {
        return activityRepository.subtractRechargeSkuStockSurplus(skuId, activityEndTime);
    }

    @Override
    public RechargeSkuStockEntity getQueueValue() {
        return activityRepository.getQueueValue();
    }

    @Override
    public void clearQueueValue() {
        activityRepository.clearQueueValue();
    }

    @Override
    public void updateRechargeSkuStock(Long skuId) {
        activityRepository.updateRechargeSkuStock(skuId);
    }

    @Override
    public void clearRechargeSkuStock(Long skuId) {
        activityRepository.clearRechargeSkuStock(skuId);
    }

}

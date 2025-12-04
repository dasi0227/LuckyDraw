package com.dasi.domain.activity.service.assemble.impl;

import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.assemble.IActivityAssemble;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class DefaultActivityAssemble implements IActivityAssemble {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public boolean assembleRechargeSkuStockByActivityId(Long activityId) {
        List<ActivitySkuEntity> activitySkuEntityList = activityRepository.queryRechargeSkuByActivityId(activityId);
        return activitySkuEntityList.stream().allMatch(rechargeSkuEntity -> assembleRechargeSkuStockBySkuId(rechargeSkuEntity.getSkuId()));
    }

    @Override
    public boolean assembleRechargeSkuStockBySkuId(Long skuId) {
        try {
            // 装配库存
            ActivitySkuEntity activitySkuEntity = activityRepository.queryRechargeSkuBySkuId(skuId);
            activityRepository.cacheRechargeSkuStockSurplus(skuId, activitySkuEntity.getStockSurplus());
            // 预热信息
            activityRepository.queryActivityByActivityId(activitySkuEntity.getActivityId());
            log.info("【装配】权益库存：activityId={}, skuId={}, surplus={}", activitySkuEntity.getActivityId(), skuId, activitySkuEntity.getStockSurplus());
            return true;
        } catch (Exception e) {
            log.error("【装配】权益库存：error={}", e.getMessage());
            return false;
        }
    }

}

package com.dasi.domain.activity.service.assemble.impl;

import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
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
        List<RechargeSkuEntity> rechargeSkuEntities = activityRepository.queryRechargeSkuByActivityId(activityId);
        return rechargeSkuEntities.stream()
                .allMatch(rechargeSkuEntity -> assembleRechargeSkuStockBySkuId(rechargeSkuEntity.getSkuId()));
    }

    @Override
    public boolean assembleRechargeSkuStockBySkuId(Long skuId) {
        RechargeSkuEntity rechargeSkuEntity = activityRepository.queryRechargeSkuBySkuId(skuId);
        activityRepository.cacheRechargeSkuStockSurplus(skuId, rechargeSkuEntity.getStockSurplus());
        activityRepository.queryActivityByActivityId(rechargeSkuEntity.getActivityId());
        activityRepository.queryRechargeQuotaByQuotaId(rechargeSkuEntity.getQuotaId());
        log.info("【活动装配】skuId = {}, surplus = {}, activityId = {}, quotaId = {}", skuId, rechargeSkuEntity.getStockSurplus(), rechargeSkuEntity.getActivityId(), rechargeSkuEntity.getQuotaId());
        return true;
    }

}

package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeQuotaEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.recharge.ISkuRecharge;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractSkuRecharge implements ISkuRecharge {

    protected final IActivityRepository activityRepository;

    public AbstractSkuRecharge(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public RechargeResult doRecharge(RechargeContext rechargeContext) {

        // 1. 参数校验
        String userId = rechargeContext.getUserId();
        String bizId = rechargeContext.getBizId();
        Long skuId = rechargeContext.getSkuId();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(bizId) || skuId == null) {
            throw new AppException("参数为空");
        }

        // 2. 查询活动的基础信息
        RechargeSkuEntity rechargeSkuEntity = activityRepository.queryRechargeSkuBySkuId(rechargeContext.getSkuId());
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(rechargeSkuEntity.getActivityId());
        RechargeQuotaEntity rechargeQuotaEntity = activityRepository.queryRechargeQuotaByQuotaId(rechargeSkuEntity.getQuotaId());

        // 3. 活动规则校验
        Boolean available = checkRechargeAvailable(rechargeSkuEntity, activityEntity, rechargeQuotaEntity);
        if (!available) {
            throw new AppException("活动规则检测失败");
        }

        // 4. 充值并保存订单
        return createRechargeOrder(rechargeContext, activityEntity, rechargeQuotaEntity);
    }

    protected abstract RechargeResult createRechargeOrder(RechargeContext rechargeContext, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity);

    protected abstract Boolean checkRechargeAvailable(RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity);

}

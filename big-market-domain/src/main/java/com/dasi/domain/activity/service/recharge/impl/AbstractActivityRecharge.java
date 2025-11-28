package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeQuotaEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.recharge.IActivityRecharge;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractActivityRecharge implements IActivityRecharge {

    protected IActivityRepository activityRepository;

    public AbstractActivityRecharge(IActivityRepository activityRepository) {
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

        // 3. 活动规则校验 TODO：暂时不处理责任链结果，后续还有更多的处理规则
        Boolean available = checkActivityAvailable(rechargeSkuEntity, activityEntity, rechargeQuotaEntity);
        if (!available) {
            return RechargeResult.builder().userId(userId).build();
        }

        // 4. 充值并保存订单
        return createRechargeOrder(rechargeContext, rechargeSkuEntity, activityEntity, rechargeQuotaEntity);
    }

    protected abstract RechargeResult createRechargeOrder(RechargeContext rechargeContext, RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity);

    protected abstract Boolean checkActivityAvailable(RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity);

}

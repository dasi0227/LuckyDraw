package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.entity.RechargeOrderEntity;
import com.dasi.domain.activity.model.io.SkuRechargeContext;
import com.dasi.domain.activity.model.io.SkuRechargeResult;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
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
    public SkuRechargeResult doSkuRecharge(SkuRechargeContext skuRechargeContext) {

        // 1. 参数校验
        String userId = skuRechargeContext.getUserId();
        String bizId = skuRechargeContext.getBizId();
        Long skuId = skuRechargeContext.getSkuId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (StringUtils.isBlank(bizId)) throw new AppException("缺少参数 bizId");
        if (skuId == null) throw new AppException("缺少参数 skuId");

        // 2. 查询活动的基础信息
        ActivitySkuEntity activitySkuEntity = activityRepository.queryRechargeSkuBySkuId(skuRechargeContext.getSkuId());
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activitySkuEntity.getActivityId());

        // 3. 活动规则校验
        Boolean available = checkRechargeAvailable(activitySkuEntity, activityEntity);
        if (Boolean.FALSE.equals(available)) {
            throw new AppException("充值信息校验失败");
        }

        // 4. 创建账户
        activityRepository.createActivityAccountIfAbsent(userId, activityEntity.getActivityId());

        // 5. 充值并保存订单
        RechargeOrderEntity rechargeOrderEntity = saveRechargeOrder(skuRechargeContext, activitySkuEntity);
        return SkuRechargeResult.builder().count(rechargeOrderEntity.getCount()).build();
    }

    protected abstract RechargeOrderEntity saveRechargeOrder(SkuRechargeContext skuRechargeContext, ActivitySkuEntity activityEntity);

    protected abstract Boolean checkRechargeAvailable(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity);

}

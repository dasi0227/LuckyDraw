package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.entity.RechargeOrderEntity;
import com.dasi.domain.activity.model.io.RechargeContext;
import com.dasi.domain.activity.model.io.RechargeResult;
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
    public RechargeResult doSkuRecharge(RechargeContext rechargeContext) {

        // 1. 参数校验
        String userId = rechargeContext.getUserId();
        String bizId = rechargeContext.getBizId();
        Long skuId = rechargeContext.getSkuId();
        if (StringUtils.isBlank(userId)) throw new AppException("（充值）缺少参数 userId");
        if (StringUtils.isBlank(bizId)) throw new AppException("（充值）缺少参数 bizId");
        if (skuId == null) throw new AppException("（充值）缺少参数 skuId");

        // 2. 查询活动的基础信息
        ActivitySkuEntity activitySkuEntity = activityRepository.queryRechargeSkuBySkuId(rechargeContext.getSkuId());
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activitySkuEntity.getActivityId());

        // 3. 活动规则校验
        Boolean available = checkRechargeAvailable(activitySkuEntity, activityEntity);
        if (Boolean.FALSE.equals(available)) {
            throw new AppException("（充值）基础信息校验失败");
        }

        // 4. 创建账户
        activityRepository.createActivityAccountIfAbsent(userId, activityEntity.getActivityId());

        // 5. 充值并保存订单
        RechargeOrderEntity rechargeOrderEntity = createRechargeOrder(rechargeContext, activitySkuEntity);
        return RechargeResult.builder()
                .orderId(rechargeOrderEntity.getOrderId())
                .count(rechargeOrderEntity.getCount())
                .build();
    }

    protected abstract RechargeOrderEntity createRechargeOrder(RechargeContext rechargeContext, ActivitySkuEntity activityEntity);

    protected abstract Boolean checkRechargeAvailable(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity);

}

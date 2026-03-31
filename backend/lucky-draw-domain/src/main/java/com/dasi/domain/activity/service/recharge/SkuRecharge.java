package com.dasi.domain.activity.service.recharge;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.entity.RechargeOrderEntity;
import com.dasi.domain.activity.model.io.SkuRechargeContext;
import com.dasi.domain.activity.model.io.SkuRechargeResult;
import com.dasi.domain.activity.model.type.RechargeState;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.chain.ActivityChainFactory;
import com.dasi.domain.activity.service.chain.IActivityChain;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class SkuRecharge implements ISkuRecharge {

    @Resource
    private IActivityRepository activityRepository;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Resource
    private ActivityChainFactory activityChainFactory;

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
        ActionChainCheckAggregate actionChainCheckAggregate = ActionChainCheckAggregate.builder()
                .activityEntity(activityEntity)
                .activitySkuEntity(activitySkuEntity)
                .build();
        IActivityChain activityChain = activityChainFactory.getRechargeActionChain();
        Boolean available = activityChain.action(actionChainCheckAggregate);
        if (Boolean.FALSE.equals(available)) {
            throw new AppException("充值信息校验失败");
        }

        // 4. 创建账户
        activityRepository.createActivityAccountIfAbsent(userId, activityEntity.getActivityId());

        // 5. 充值并保存订单
        RechargeOrderEntity rechargeOrderEntity = RechargeOrderEntity.builder()
                .orderId(uniqueIdGenerator.nextRechargeOrderId())
                .bizId(skuRechargeContext.getBizId())
                .activityId(activitySkuEntity.getActivityId())
                .userId(skuRechargeContext.getUserId())
                .skuId(skuRechargeContext.getSkuId())
                .count(activitySkuEntity.getCount())
                .rechargeState(RechargeState.CREATED)
                .rechargeTime(LocalDateTime.now())
                .build();
        activityRepository.saveRechargeOrder(rechargeOrderEntity);
        return SkuRechargeResult.builder().count(rechargeOrderEntity.getCount()).build();
    }

}

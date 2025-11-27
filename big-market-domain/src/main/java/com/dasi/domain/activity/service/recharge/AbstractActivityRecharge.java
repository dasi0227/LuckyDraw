package com.dasi.domain.activity.service.recharge;

import com.dasi.domain.activity.model.dto.RechargeContext;
import com.dasi.domain.activity.model.dto.RechargeResult;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivityQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
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
    public RechargeResult skuRecharge(RechargeContext rechargeContext) {
        // 1. 参数校验
        String userId = rechargeContext.getUserId();
        String bizId = rechargeContext.getBizId();
        Long skuId = rechargeContext.getSkuId();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(bizId) || skuId == null) {
            throw new AppException("参数为空");
        }

        // 2. 查询活动的基础信息
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuBySkuId(rechargeContext.getSkuId());
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activitySkuEntity.getActivityId());
        ActivityQuotaEntity activityQuotaEntity = activityRepository.queryActivityQuotaByActivityQuotaId(activitySkuEntity.getActivityQuotaId());

        // 3. 活动规则校验 TODO：暂时不处理责任链结果，后续还有更多的处理规则
        Boolean success = checkInvalid(activitySkuEntity, activityEntity, activityQuotaEntity);
        if (!success) {
            return RechargeResult.builder().userId(userId).build();
        }

        // 4. 充值并保存订单
        return createRechargeOrder(rechargeContext, activitySkuEntity, activityEntity, activityQuotaEntity);
    }

    protected abstract RechargeResult createRechargeOrder(RechargeContext rechargeContext, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityQuotaEntity activityQuotaEntity);

    protected abstract Boolean checkInvalid(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityQuotaEntity activityQuotaEntity);

}

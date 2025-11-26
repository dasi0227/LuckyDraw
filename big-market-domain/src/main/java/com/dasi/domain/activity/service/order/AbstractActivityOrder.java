package com.dasi.domain.activity.service.order;

import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.model.dto.SkuOrder;
import com.dasi.domain.activity.model.dto.SkuRecharge;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractActivityOrder implements IActivityOrder {

    protected IActivityRepository activityRepository;

    public AbstractActivityOrder(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public String createSkuRechargeOrder(SkuRecharge skuRecharge) {
        // 1. 参数校验
        String userId = skuRecharge.getUserId();
        String bizId = skuRecharge.getBizId();
        Long sku = skuRecharge.getSku();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(bizId) || sku == null) {
            throw new AppException("参数为空");
        }

        // 2. 查询基础信息
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuBySku(skuRecharge.getSku());
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activitySkuEntity.getActivityId());
        ActivityCountEntity activityCountEntity = activityRepository.queryActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        // 3. 活动规则校验
        // TODO：暂时不处理责任链结果，后续还有更多的处理规则
        checkInvalid(activitySkuEntity, activityEntity, activityCountEntity);

        // 4. 构建订单聚合对象
        SkuOrder skuOrder = buildSkuOrder(skuRecharge, activityEntity, activityCountEntity);

        // 5. 保存并返回单号
        saveSkuOrder(skuOrder);
        return skuOrder.getActivityOrderEntity().getOrderId();
    }

    protected abstract void saveSkuOrder(SkuOrder skuOrder);

    protected abstract SkuOrder buildSkuOrder(SkuRecharge skuRecharge, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

    protected abstract void checkInvalid(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}

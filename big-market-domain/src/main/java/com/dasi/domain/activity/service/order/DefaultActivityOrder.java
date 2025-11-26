package com.dasi.domain.activity.service.order;

import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.type.OrderState;
import com.dasi.domain.activity.model.dto.SkuOrder;
import com.dasi.domain.activity.model.dto.SkuRecharge;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.action.chain.ActionChainFactory;
import com.dasi.domain.activity.service.action.chain.IActionChain;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DefaultActivityOrder extends AbstractActivityOrder {

    public DefaultActivityOrder(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Resource
    private ActionChainFactory actionChainFactory;

    @Override
    public void checkInvalid(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        IActionChain actionChain = actionChainFactory.getFirstActionChain();
        actionChain.action(activitySkuEntity, activityEntity, activityCountEntity);
    }

    @Override
    protected SkuOrder buildSkuOrder(SkuRecharge skuRecharge, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        // 创建订单
        ActivityOrderEntity activityOrderEntity = ActivityOrderEntity.builder()
                .orderId(RandomStringUtils.randomNumeric(12))
                .bizId(skuRecharge.getBizId())
                .userId(skuRecharge.getUserId())
                .sku(skuRecharge.getSku())
                .activityId(activityEntity.getActivityId())
                .activityName(activityEntity.getActivityName())
                .strategyId(activityEntity.getStrategyId())
                .totalCount(activityCountEntity.getTotalCount())
                .monthCount(activityCountEntity.getMonthCount())
                .dayCount(activityCountEntity.getDayCount())
                .orderTime(LocalDateTime.now())
                .state(OrderState.COMPLETED.getCode())
                .build();

                return SkuOrder.builder()
                        .userId(skuRecharge.getUserId())
                        .activityId(activityEntity.getActivityId())
                        .totalCount(activityCountEntity.getTotalCount())
                        .dayCount(activityCountEntity.getDayCount())
                        .monthCount(activityCountEntity.getMonthCount())
                        .activityOrderEntity(activityOrderEntity)
                        .build();
    }


    @Override
    protected void saveSkuOrder(SkuOrder skuOrder) {
        activityRepository.saveOrder(skuOrder);
    }


}

package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.dto.RaffleContext;
import com.dasi.domain.activity.model.aggregate.RaffleOrderAggregate;
import com.dasi.domain.activity.model.dto.RaffleResult;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RaffleOrderEntity;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Slf4j
public abstract class AbstractActivityRaffle implements IActivityRaffle {

    protected final IActivityRepository activityRepository;

    public AbstractActivityRaffle(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public RaffleResult doActivityRaffle(RaffleContext raffleContext) {

        // 1. 参数校验
        String userId = raffleContext.getUserId();
        Long activityId = raffleContext.getActivityId();
        if (StringUtils.isBlank(userId) || activityId == null) {
            throw new AppException("参数为空");
        }

        // 2. 活动与用户校验
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activityId);
        RaffleOrderAggregate raffleOrderAggregate = checkRaffleAvailable(userId, activityEntity);
        if (raffleOrderAggregate == null) {
            throw new AppException("活动与用户校验失败");
        }

        // 3. 查询还未执行完成的抽奖
        RaffleOrderEntity raffleOrderEntity = activityRepository.queryUnusedRaffleOrder(userId, activityId);
        if (raffleOrderEntity != null) {
            return RaffleResult.builder()
                    .orderId(raffleOrderEntity.getOrderId())
                    .strategyId(raffleOrderEntity.getStrategyId())
                    .build();
        } else {
            raffleOrderEntity = RaffleOrderEntity.builder()
                    .orderId(RandomStringUtils.randomNumeric(12))
                    .userId(userId)
                    .activityId(activityId)
                    .strategyId(activityEntity.getStrategyId())
                    .raffleState(RaffleState.CREATED.getCode())
                    .raffleTime(LocalDateTime.now())
                    .build();
        }

        // 4. 保存订单
        raffleOrderAggregate.setRaffleOrderEntity(raffleOrderEntity);
        saveRaffleOrder(raffleOrderAggregate);

        return RaffleResult.builder()
                .orderId(raffleOrderEntity.getOrderId())
                .strategyId(raffleOrderEntity.getStrategyId())
                .build();
    }

    protected abstract void saveRaffleOrder(RaffleOrderAggregate raffleOrderAggregate);

    protected abstract RaffleOrderAggregate checkRaffleAvailable(String userId, ActivityEntity activityEntity);

}

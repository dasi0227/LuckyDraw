package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RaffleOrderEntity;
import com.dasi.domain.activity.model.io.RaffleContext;
import com.dasi.domain.activity.model.io.RaffleResult;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
public abstract class AbstractActivityRaffle implements IActivityRaffle {

    protected final IActivityRepository activityRepository;

    public AbstractActivityRaffle(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public RaffleResult doActivityRaffle(RaffleContext raffleContext) {

        // 1. 参数校验
        String userId = raffleContext.getUserId();
        Long activityId = raffleContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("（抽奖）缺少参数 userId");
        if (activityId == null) throw new AppException("（抽奖）缺少参数 activityId");

        // 2. 活动与用户校验
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activityId);
        Boolean available = checkRaffleAvailable(userId, activityEntity);
        if (Boolean.FALSE.equals(available)) {
            throw new AppException("（抽奖）基础信息校验失败");
        }

        // 3. 查询还未执行完成的抽奖，如果没有则新建
        RaffleOrderEntity raffleOrderEntity = activityRepository.queryUnusedRaffleOrder(userId, activityId);
        if (raffleOrderEntity != null) {
            log.info("【抽奖】存在未完成的抽奖：orderId={}, raffle_state={}", raffleOrderEntity.getOrderId(), raffleOrderEntity.getRaffleState());
            return RaffleResult.builder()
                    .orderId(raffleOrderEntity.getOrderId())
                    .strategyId(raffleOrderEntity.getStrategyId())
                    .build();
        } else {
            raffleOrderEntity = RaffleOrderEntity.builder()
                    .orderId(uniqueIdGenerator.nextRaffleOrderId())
                    .userId(userId)
                    .activityId(activityId)
                    .strategyId(activityEntity.getStrategyId())
                    .raffleState(RaffleState.CREATED)
                    .raffleTime(LocalDateTime.now())
                    .build();
        }

        // 4. 保存订单
        saveRaffleOrder(raffleOrderEntity);

        return RaffleResult.builder()
                .orderId(raffleOrderEntity.getOrderId())
                .strategyId(raffleOrderEntity.getStrategyId())
                .build();
    }

    protected abstract void saveRaffleOrder(RaffleOrderEntity raffleOrderEntity);

    protected abstract Boolean checkRaffleAvailable(String userId, ActivityEntity activityEntity);

}

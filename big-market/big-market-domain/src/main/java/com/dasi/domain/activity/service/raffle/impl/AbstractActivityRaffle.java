package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RaffleOrderEntity;
import com.dasi.domain.activity.model.io.RaffleContext;
import com.dasi.domain.activity.model.io.RaffleResult;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.domain.common.IRedisLock;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import com.dasi.types.exception.BusinessException;
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

    @Resource
    private IRedisLock redisLock;

    @Override
    public RaffleResult doActivityRaffle(RaffleContext raffleContext) {

        // 参数校验
        String userId = raffleContext.getUserId();
        Long activityId = raffleContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        String lockKey = RedisKey.LOCK_RAFFLE_KEY + activityId + Delimiter.COLON + userId;
        boolean isLock = false;

        try {
            isLock = redisLock.tryLock(lockKey);
            if (!isLock) {
                throw new BusinessException("当前系统繁忙，请稍后再试");
            }

            // 活动与用户校验
            ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activityId);
            Boolean available = checkRaffleAvailable(userId, activityEntity);
            if (Boolean.FALSE.equals(available)) {
                throw new AppException("活动信息校验失败");
            }

            // 查询还未执行完成的抽奖，如果没有则新建
            RaffleOrderEntity raffleOrderEntity = activityRepository.queryUnusedRaffleOrder(userId, activityId);
            if (raffleOrderEntity != null) {
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

            // 保存订单
            saveRaffleOrder(raffleOrderEntity);

            return RaffleResult.builder()
                    .orderId(raffleOrderEntity.getOrderId())
                    .strategyId(raffleOrderEntity.getStrategyId())
                    .build();
        } finally {
            if (isLock) {
                redisLock.unlock(lockKey);
            }
        }
    }

    protected abstract void saveRaffleOrder(RaffleOrderEntity raffleOrderEntity);

    protected abstract Boolean checkRaffleAvailable(String userId, ActivityEntity activityEntity);

}

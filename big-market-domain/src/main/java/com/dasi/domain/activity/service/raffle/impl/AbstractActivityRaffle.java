package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.dto.RaffleContext;
import com.dasi.domain.activity.model.dto.RaffleOrderAggregate;
import com.dasi.domain.activity.model.dto.RaffleResult;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RaffleOrderEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractActivityRaffle implements IActivityRaffle {

    protected IActivityRepository activityRepository;

    public AbstractActivityRaffle(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public RaffleResult doRaffle(RaffleContext raffleContext) {

        // 1. 参数校验
        String userId = raffleContext.getUserId();
        Long activityId = raffleContext.getActivityId();
        if (StringUtils.isBlank(userId) || activityId == null) {
            throw new AppException("参数为空");
        }

        // 2. 活动校验
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activityId);
        Boolean available = checkActivityAvailable(activityEntity);
        if (!available) {
            return RaffleResult.builder().build();
        }

        // 3. 查询还未执行完成的抽奖
        RaffleOrderEntity raffleOrderEntity = activityRepository.queryUnusedRaffleOrder(userId, activityId);
        if (raffleOrderEntity != null) {
            return RaffleResult.builder().raffleOrderEntity(raffleOrderEntity).build();
        }

        // 4. 用户校验
        RaffleOrderAggregate raffleOrderAggregate = checkAccountAvailable(userId, activityId);

        // 5. 构建订单
        raffleOrderEntity = createRaffleOrder(userId, activityId);
        raffleOrderAggregate.setRaffleOrderEntity(raffleOrderEntity);

        // 6. 构造聚合对象
        activityRepository.saveRaffleOrder(raffleOrderAggregate);

        return RaffleResult.builder().raffleOrderEntity(raffleOrderEntity).build();
    }

    protected abstract RaffleOrderEntity createRaffleOrder(String userId, Long activityId);

    protected abstract RaffleOrderAggregate checkAccountAvailable(String userId, Long activityId);

    protected abstract Boolean checkActivityAvailable(ActivityEntity activityEntity);

}

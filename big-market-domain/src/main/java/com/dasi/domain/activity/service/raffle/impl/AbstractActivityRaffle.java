package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.dto.RaffleContext;
import com.dasi.domain.activity.model.dto.RaffleResult;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.raffle.IActivityRaffle;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractActivityRaffle implements IActivityRaffle {

    protected IActivityRepository activityRepository;

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

        // 2. 活动校验
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activityId);
        Boolean success = checkRaffleValid(activityEntity);

        // 3. 查询

        return null;
    }

    protected abstract Boolean checkRaffleValid(ActivityEntity activityEntity);

}

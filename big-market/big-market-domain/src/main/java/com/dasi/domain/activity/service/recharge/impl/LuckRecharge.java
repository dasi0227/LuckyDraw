package com.dasi.domain.activity.service.recharge.impl;

import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.model.io.FortuneContext;
import com.dasi.domain.activity.model.io.FortuneResult;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.recharge.ILuckRecharge;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LuckRecharge implements ILuckRecharge {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public FortuneResult doFortune(FortuneContext fortuneContext) {

        // 1. 参数校验
        String userId = fortuneContext.getUserId();
        Long activityId = fortuneContext.getActivityId();
        Integer luck = fortuneContext.getLuck();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");
        if (luck == null) throw new AppException("缺少参数 luck");
        if (luck <= 0) throw new AppException("参数 luck 非法");

        ActivityAccountEntity activityAccountEntity = ActivityAccountEntity.builder()
                .userId(userId)
                .activityId(activityId)
                .accountLuck(luck)
                .build();

        Integer accountLuck = activityRepository.increaseActivityAccountLuck(activityAccountEntity);
        return FortuneResult.builder().accountLuck(accountLuck).build();
    }
}

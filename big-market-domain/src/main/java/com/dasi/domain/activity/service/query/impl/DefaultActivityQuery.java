package com.dasi.domain.activity.service.query.impl;

import com.dasi.domain.activity.model.entity.ActivityAccountDayEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountMonthEntity;
import com.dasi.domain.activity.model.io.QueryAccountContext;
import com.dasi.domain.activity.model.io.QueryAccountResult;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.query.IActivityQuery;
import com.dasi.types.exception.AppException;
import com.dasi.types.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultActivityQuery implements IActivityQuery {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public QueryAccountResult queryActivityAccount(QueryAccountContext queryAccountContext) {

        // 1. 参数校验
        String userId = queryAccountContext.getUserId();
        Long activityId = queryAccountContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("（抽奖）缺少参数 userId");
        if (activityId == null) throw new AppException("（抽奖）缺少参数 activityId");

        // 2. 创建账户
        activityRepository.createActivityAccountIfAbsent(userId, activityId);

        // 3. 查询账户信息
        String monthKey = TimeUtil.thisMonth(true);
        String dayKey = TimeUtil.thisDay(true);
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccount(userId, activityId);
        ActivityAccountMonthEntity activityAccountMonth = activityRepository.queryActivityAccountMonth(userId, activityId, monthKey);
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, dayKey);

        // 4. 构建
        return QueryAccountResult.builder()
                .totalAllocate(activityAccountEntity.getTotalAllocate())
                .totalSurplus(activityAccountEntity.getTotalSurplus())
                .monthKey(monthKey)
                .monthLimit(activityAccountEntity.getMonthLimit())
                .monthAllocate(activityAccountMonth.getMonthAllocate())
                .monthSurplus(activityAccountMonth.getMonthSurplus())
                .dayKey(dayKey)
                .dayLimit(activityAccountEntity.getDayLimit())
                .dayAllocate(activityAccountDayEntity.getDayAllocate())
                .daySurplus(activityAccountDayEntity.getDaySurplus())
                .build();
    }

}

package com.dasi.domain.activity.service.query;

import com.dasi.domain.activity.model.entity.ActivityAccountDayEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountMonthEntity;
import com.dasi.domain.activity.model.io.QueryActivityAccountContext;
import com.dasi.domain.activity.model.io.QueryActivityAccountResult;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.types.exception.AppException;
import com.dasi.types.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ActivityQuery implements IActivityQuery {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public QueryActivityAccountResult queryActivityAccount(QueryActivityAccountContext queryActivityAccountContext) {

        // 1. 参数校验
        String userId = queryActivityAccountContext.getUserId();
        Long activityId = queryActivityAccountContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        // 2. 创建账户
        activityRepository.createActivityAccountIfAbsent(userId, activityId);

        // 3. 查询账户信息
        String monthKey = TimeUtil.thisMonth(true);
        String dayKey = TimeUtil.thisDay(true);
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccount(userId, activityId);
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, monthKey);
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, dayKey);

        // 4. 构建
        return QueryActivityAccountResult.builder()
                .accountPoint(activityAccountEntity.getAccountPoint())
                .totalSurplus(activityAccountEntity.getTotalSurplus())
                .monthSurplus(activityAccountMonthEntity.getMonthSurplus())
                .daySurplus(activityAccountDayEntity.getDaySurplus())
                .monthPending(activityAccountMonthEntity.getMonthLimit() - activityAccountMonthEntity.getMonthAllocate())
                .dayPending(activityAccountDayEntity.getDayLimit() - activityAccountDayEntity.getDayAllocate())
                .build();
    }

}

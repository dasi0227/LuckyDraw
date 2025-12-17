package com.dasi.domain.activity.service.query;

import com.dasi.domain.activity.model.io.QueryActivityAccountContext;
import com.dasi.domain.activity.model.io.QueryActivityAccountResult;
import com.dasi.domain.activity.model.io.QueryActivityInfoContext;
import com.dasi.domain.activity.model.io.QueryActivityInfoResult;
import com.dasi.domain.activity.model.io.QueryActivityListResult;
import com.dasi.domain.activity.model.vo.AccountSnapshot;
import com.dasi.domain.activity.model.vo.ActivitySnapshot;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
        AccountSnapshot accountSnapshot = activityRepository.queryAccountSnapshot(userId, activityId);

        // 4. 构建
        return QueryActivityAccountResult.builder()
                .accountPoint(accountSnapshot.getAccountPoint())
                .totalSurplus(accountSnapshot.getTotalSurplus())
                .monthSurplus(accountSnapshot.getMonthSurplus())
                .daySurplus(accountSnapshot.getDaySurplus())
                .monthPending(accountSnapshot.getMonthLimit() - accountSnapshot.getMonthAllocate())
                .dayPending(accountSnapshot.getDayLimit() - accountSnapshot.getDayAllocate())
                .build();
    }

    @Override
    public QueryActivityInfoResult queryActivityInfo(QueryActivityInfoContext queryActivityInfoContext) {

        Long activityId = queryActivityInfoContext.getActivityId();
        if (activityId == null) throw new AppException("缺少参数 activityId");

        ActivitySnapshot activitySnapshot = activityRepository.queryActivitySnapshot(activityId);

        return QueryActivityInfoResult.builder()
                .activityName(activitySnapshot.getActivityName())
                .activityDesc(activitySnapshot.getActivityDesc())
                .activityBeginTime(activitySnapshot.getActivityBeginTime())
                .activityEndTime(activitySnapshot.getActivityEndTime())
                .activityAccountCount(activitySnapshot.getActivityAccountCount())
                .activityAwardCount(activitySnapshot.getActivityAwardCount())
                .activityRaffleCount(activitySnapshot.getActivityRaffleCount())
                .build();
    }

    @Override
    public List<QueryActivityListResult> queryActivityList() {
        return activityRepository.queryActivityList().stream()
                .map(activityEntity -> QueryActivityListResult.builder()
                        .activityId(activityEntity.getActivityId())
                        .activityName(activityEntity.getActivityName())
                        .activityDesc(activityEntity.getActivityDesc())
                        .build())
                .collect(Collectors.toList());
    }

}

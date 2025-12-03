package com.dasi.domain.activity.service.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.aggregate.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.ActivityAccountDayEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountMonthEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component(ActionModel.ACCOUNT_INFO)
public class AccountInfoChain extends AbstractActivityChain {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {

        String userId = actionChainCheckAggregate.getUserId();
        Long activityId = actionChainCheckAggregate.getActivityId();

        /* ========= 1. 查询总余额 ========= */
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccount(userId, activityId);
        if (activityAccountEntity.getTotalSurplus() <= 0) {
            log.info("【检查】account_info 拦截（账户总余额不足）：userId={}, activityId={}, surplus={}", userId, activityId, activityAccountEntity.getTotalSurplus());
            return false;
        }

        /* ========= 2. 查询月余额 ========= */
        String month = TimeUtil.thisMonth(true);
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, month);
        if (activityAccountMonthEntity != null && activityAccountMonthEntity.getMonthSurplus() <= 0) {
            log.info("【检查】account_info 拦截（账户月余额不足）：userId={}, activityId={}, month={}, monthSurplus={}", userId, activityId, month, activityAccountMonthEntity.getMonthSurplus());
            return false;
        } else if (activityAccountMonthEntity == null) {
            activityAccountMonthEntity = ActivityAccountMonthEntity.builder()
                    .activityId(activityAccountEntity.getActivityId())
                    .userId(activityAccountEntity.getUserId())
                    .month(month)
                    .monthAllocate(activityAccountEntity.getMonthAllocate())
                    .monthSurplus(activityAccountEntity.getMonthSurplus())
                    .build();
        }

        /* ========= 3. 查询日余额 ========= */
        String day = TimeUtil.thisDay(true);
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, day);
        if (activityAccountDayEntity != null && activityAccountDayEntity.getDaySurplus() <= 0) {
            log.info("【检查】account_info 拦截（账户日余额不足）：userId={}, activityId={}, day={}, daySurplus={}", userId, activityId, day, activityAccountDayEntity.getDaySurplus());
            return false;
        } else if (activityAccountDayEntity == null) {
            activityAccountDayEntity = ActivityAccountDayEntity.builder()
                    .activityId(activityAccountEntity.getActivityId())
                    .userId(activityAccountEntity.getUserId())
                    .day(day)
                    .dayAllocate(activityAccountEntity.getDayAllocate())
                    .daySurplus(activityAccountEntity.getDaySurplus())
                    .build();
        }

        // 构造聚合
        RaffleOrderAggregate raffleOrderAggregate = RaffleOrderAggregate.builder()
                .userId(userId)
                .activityId(activityId)
                .activityAccountEntity(activityAccountEntity)
                .activityAccountMonthEntity(activityAccountMonthEntity)
                .activityAccountDayEntity(activityAccountDayEntity)
                .build();
        actionChainCheckAggregate.setRaffleOrderAggregate(raffleOrderAggregate);
        log.info("【检查】account_info 放行：userId={}, activityId={}, total={}, month={}, day={}",
                userId, activityId,
                activityAccountEntity.getTotalSurplus(), activityAccountEntity.getMonthSurplus(), activityAccountEntity.getDaySurplus());

        return next().action(actionChainCheckAggregate);
    }

}

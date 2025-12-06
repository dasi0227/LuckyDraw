package com.dasi.domain.activity.service.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
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
        if (activityAccountEntity == null) {
            log.info("【检查】ACCOUNT_INFO 拦截（账户不存在）：userId={}, activityId={}", userId, activityId);
            return false;
        }
        if (activityAccountEntity.getTotalSurplus() <= 0) {
            log.info("【检查】ACCOUNT_INFO 拦截（账户总余额不足）：userId={}, activityId={}, surplus={}", userId, activityId, activityAccountEntity.getTotalSurplus());
            return false;
        }

        /* ========= 2. 查询月余额 ========= */
        String month = TimeUtil.thisMonth(true);
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, month);
        if (activityAccountMonthEntity == null) {
            log.info("【检查】ACCOUNT_INFO 拦截（月账户不存在）：userId={}, activityId={}, month={}", userId, activityId, month);
            return false;
        }
        if (activityAccountMonthEntity.getMonthSurplus() <= 0) {
            log.info("【检查】ACCOUNT_INFO 拦截（账户月余额不足）：userId={}, activityId={}, month={}, monthSurplus={}", userId, activityId, month, activityAccountMonthEntity.getMonthSurplus());
            return false;
        }

        /* ========= 3. 查询日余额 ========= */
        String day = TimeUtil.thisDay(true);
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, day);
        if (activityAccountDayEntity == null) {
            log.info("【检查】ACCOUNT_INFO 拦截（日账户不存在）：userId={}, activityId={}, day={}", userId, activityId, day);
            return false;
        }
        if (activityAccountMonthEntity.getMonthSurplus() <= 0) {
            log.info("【检查】ACCOUNT_INFO 拦截（账户日余额不足）：userId={}, activityId={}, day={}, daySurplus={}", userId, activityId, day, activityAccountDayEntity.getDaySurplus());
            return false;
        }

        log.info("【检查】ACCOUNT_INFO 放行：userId={}, activityId={}, total_surplus={}, month={}, month_surplus={}, day={}, day_surplus={}",
                userId, activityId, activityAccountEntity.getTotalSurplus(),
                month, activityAccountMonthEntity.getMonthSurplus(),
                day, activityAccountDayEntity.getDaySurplus());

        return next().action(actionChainCheckAggregate);
    }

}

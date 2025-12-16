package com.dasi.domain.activity.service.chain.impl;

import com.dasi.domain.activity.annotation.ActionModelConfig;
import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.ActivityAccountDayEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountMonthEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.types.constant.ExceptionMessage;
import com.dasi.types.exception.BusinessException;
import com.dasi.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@ActionModelConfig(actionModel = ActionModel.ACCOUNT_INFO)
@Component
public class AccountInfoChain extends AbstractActivityChain {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {

        String userId = actionChainCheckAggregate.getUserId();
        Long activityId = actionChainCheckAggregate.getActivityId();

        /* ========= 1. 查询总抽奖次数 ========= */
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccount(userId, activityId);
        if (activityAccountEntity == null) {
            log.info("【活动】ACCOUNT_INFO 拦截（账户不存在）：userId={}, activityId={}", userId, activityId);
            throw new BusinessException(ExceptionMessage.ACCOUNT_NOT_EXISTS);
        }
        if (activityAccountEntity.getTotalSurplus() <= 0) {
            log.info("【活动】ACCOUNT_INFO 拦截（账户总抽奖次数不足）：userId={}, activityId={}, surplus={}", userId, activityId, activityAccountEntity.getTotalSurplus());
            throw new BusinessException(ExceptionMessage.TOTAL_SURPLUS_NOT_ENOUGH);
        }

        /* ========= 2. 查询月抽奖次数 ========= */
        String month = TimeUtil.thisMonth(true);
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, month);
        if (activityAccountMonthEntity == null) {
            log.info("【活动】ACCOUNT_INFO 拦截（月账户不存在）：userId={}, activityId={}, month={}", userId, activityId, month);
            throw new BusinessException(ExceptionMessage.MONTH_ACCOUNT_NOT_EXISTS);
        }
        if (activityAccountMonthEntity.getMonthSurplus() <= 0) {
            log.info("【活动】ACCOUNT_INFO 拦截（账户月抽奖次数不足）：userId={}, activityId={}, month={}, monthSurplus={}", userId, activityId, month, activityAccountMonthEntity.getMonthSurplus());
            throw new BusinessException(ExceptionMessage.MONTH_SURPLUS_NOT_ENOUGH);
        }

        /* ========= 3. 查询日抽奖次数 ========= */
        String day = TimeUtil.thisDay(true);
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, day);
        if (activityAccountDayEntity == null) {
            log.info("【活动】ACCOUNT_INFO 拦截（日账户不存在）：userId={}, activityId={}, day={}", userId, activityId, day);
            throw new BusinessException(ExceptionMessage.DAY_ACCOUNT_NOT_EXISTS);
        }
        if (activityAccountDayEntity.getDaySurplus() <= 0) {
            log.info("【活动】ACCOUNT_INFO 拦截（账户日抽奖次数不足）：userId={}, activityId={}, day={}, daySurplus={}", userId, activityId, day, activityAccountDayEntity.getDaySurplus());
            throw new BusinessException(ExceptionMessage.DAY_SURPLUS_NOT_ENOUGH);
        }

        log.info("【活动】ACCOUNT_INFO 放行：userId={}, activityId={}, total_surplus={}, month={}, month_surplus={}, day={}, day_surplus={}",
                userId, activityId, activityAccountEntity.getTotalSurplus(),
                month, activityAccountMonthEntity.getMonthSurplus(),
                day, activityAccountDayEntity.getDaySurplus());

        return next().action(actionChainCheckAggregate);
    }

}

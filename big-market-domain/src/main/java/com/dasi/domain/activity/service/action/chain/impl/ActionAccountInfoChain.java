package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.aggregate.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.ActivityAccountDayEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountEntity;
import com.dasi.domain.activity.model.entity.ActivityAccountMonthEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.repository.IActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component(ActionModel.ACCOUNT_INFO)
public class ActionAccountInfoChain extends AbstractActionChain {

    @Resource
    private IActivityRepository activityRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DAY_FORMATTER   = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {

        String userId = actionChainCheckAggregate.getUserId();
        Long activityId = actionChainCheckAggregate.getActivityId();

        /* ========= 1. 查询总余额 ========= */
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccount(userId, activityId);
        if (activityAccountEntity == null || activityAccountEntity.getTotalSurplus() <= 0) {
            log.info("【活动责任链 - account_info】账户总余额不足：userId={}, activityId={}", userId, activityId);
            return false;
        }

        /* ========= 2. 查询月余额 ========= */
        String month = LocalDate.now().format(MONTH_FORMATTER);
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, month);
        if (activityAccountMonthEntity != null && activityAccountMonthEntity.getMonthSurplus() <= 0) {
            log.info("【抽奖-责任链-account】账户月余额不足：userId={}, activityId={}, month={}", userId, activityId, month);
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
        String day = LocalDate.now().format(DAY_FORMATTER);
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, day);
        if (activityAccountDayEntity != null && activityAccountDayEntity.getDaySurplus() <= 0) {
            log.info("【抽奖-责任链-account】账户日余额不足：userId={}, activityId={}, day={}", userId, activityId, day);
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
        log.info("【活动责任链 - account_info】账户余额充足：userId={}, activityId={}, total={}, month={}, day={}",
                userId, activityId,
                activityAccountEntity.getTotalSurplus(), activityAccountEntity.getMonthSurplus(), activityAccountEntity.getDaySurplus());

        return next().action(actionChainCheckAggregate);
    }

}

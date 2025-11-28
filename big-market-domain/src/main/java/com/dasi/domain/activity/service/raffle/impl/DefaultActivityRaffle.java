package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.dto.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.action.chain.ActionChainFactory;
import com.dasi.domain.activity.service.action.chain.IActionChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Slf4j
@Service
public class DefaultActivityRaffle extends AbstractActivityRaffle {

    private final SimpleDateFormat monthDateFormat = new SimpleDateFormat("yyyy-MM");

    private final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private ActionChainFactory actionChainFactory;

    @Resource
    private IActivityRepository activityRepository;

    public DefaultActivityRaffle(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    protected RaffleOrderEntity createRaffleOrder(String userId, Long activityId) {
        ActivityEntity activityEntity = activityRepository.queryActivityByActivityId(activityId);
        return RaffleOrderEntity.builder()
                .orderId(RandomStringUtils.randomNumeric(12))
                .userId(userId)
                .activityId(activityId)
                .strategyId(activityEntity.getStrategyId())
                .raffleState(RaffleState.CREATED.getCode())
                .raffleTime(LocalDateTime.now())
                .build();
    }

    @Override
    protected Boolean checkActivityAvailable(ActivityEntity activityEntity) {
        IActionChain actionChain = actionChainFactory.getRaffleActionChain();
        return actionChain.action(null, activityEntity, null);
    }

    @Override
    protected RaffleOrderAggregate checkAccountAvailable(String userId, Long activityId) {

        // 1. 查询用户额度
        ActivityAccountEntity activityAccountEntity = activityRepository.queryActivityAccount(userId, activityId);
        if (activityAccountEntity == null || activityAccountEntity.getTotalSurplus() <= 0) {
            log.info("【xxx】用户当前的账户额度不足");
            return null;
        }

        // 2. 查询月额度
        String month = monthDateFormat.format(LocalDateTime.now());
        ActivityAccountMonthEntity activityAccountMonthEntity = activityRepository.queryActivityAccountMonth(userId, activityId, month);
        if (activityAccountMonthEntity != null && activityAccountMonthEntity.getMonthSurplus() <= 0) {
            log.info("【xxx】用户当前的月额度不足");
            return null;
        } else if (activityAccountMonthEntity == null) {
            activityAccountMonthEntity = ActivityAccountMonthEntity.builder()
                    .activityId(activityAccountEntity.getActivityId())
                    .userId(activityAccountEntity.getUserId())
                    .month(month)
                    .monthAllocate(activityAccountEntity.getMonthAllocate())
                    .monthSurplus(activityAccountEntity.getMonthSurplus())
                    .build();
        }

        // 3. 查询日额度
        String day = dayDateFormat.format(LocalDateTime.now());
        ActivityAccountDayEntity activityAccountDayEntity = activityRepository.queryActivityAccountDay(userId, activityId, day);
        if (activityAccountDayEntity != null && activityAccountDayEntity.getDaySurplus() <= 0) {
            log.info("【xxx】用户当前的日额度不足");
            return null;
        } else if (activityAccountDayEntity == null) {
            activityAccountDayEntity = ActivityAccountDayEntity.builder()
                    .activityId(activityAccountEntity.getActivityId())
                    .userId(activityAccountEntity.getUserId())
                    .day(day)
                    .dayAllocate(activityAccountEntity.getDayAllocate())
                    .daySurplus(activityAccountEntity.getDaySurplus())
                    .build();
        }

        // 4. 返回对象

        return RaffleOrderAggregate.builder()
                .userId(userId)
                .activityId(activityId)
                .activityAccountEntity(activityAccountEntity)
                .activityAccountMonthEntity(activityAccountMonthEntity)
                .activityAccountDayEntity(activityAccountDayEntity)
                .build();
    }

}

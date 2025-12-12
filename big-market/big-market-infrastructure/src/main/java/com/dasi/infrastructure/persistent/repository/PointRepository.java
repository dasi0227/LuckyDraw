package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.point.model.entity.ActivityAccountEntity;
import com.dasi.domain.point.model.entity.TaskEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.type.AwardSource;
import com.dasi.domain.point.model.type.TaskState;
import com.dasi.domain.point.model.type.TradeState;
import com.dasi.domain.point.model.type.TradeType;
import com.dasi.domain.point.model.vo.AccountSnapshot;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PointRepository implements IPointRepository {

    @Resource
    private ITradeDao tradeDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private IAwardDao awardDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IUserAwardDao userAwardDao;

    @Resource
    private IActivityAccountDao activityAccountDao;

    @Resource
    private IActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IActivityAccountMonthDao activityAccountMonthDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IRedisService redisService;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public Integer queryActivityAccountPoint(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);
            ActivityAccount activityAccount = new ActivityAccount();
            activityAccount.setUserId(userId);
            activityAccount.setActivityId(activityId);
            return activityAccountDao.queryActivityAccountPoint(activityAccount);
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public List<TradeEntity> queryActivityConvertList(Long activityId) {
        String cacheKey = RedisKey.TRADE_LIST_KEY + activityId;
        List<TradeEntity> tradeEntityList = redisService.getValue(cacheKey);
        if (tradeEntityList != null && !tradeEntityList.isEmpty()) {
            return tradeEntityList;
        }

        List<Trade> tradeList = tradeDao.queryActivityConvertList(activityId);
        tradeEntityList = tradeList.stream()
                .map(trade -> TradeEntity.builder()
                        .tradeId(trade.getTradeId())
                        .activityId(trade.getActivityId())
                        .tradeType(TradeType.valueOf(trade.getTradeType()))
                        .tradePoint(trade.getTradePoint())
                        .tradeValue(trade.getTradeValue())
                        .tradeName(trade.getTradeName())
                        .tradeDesc(trade.getTradeDesc())
                        .build())
                .collect(Collectors.toList());

        redisService.setValue(cacheKey, tradeEntityList);
        return tradeEntityList;
    }

    @Override
    public TradeEntity queryTradeByTradeId(Long tradeId) {
        String cacheKey = RedisKey.TRADE_KEY + tradeId;
        TradeEntity tradeEntity = redisService.getValue(cacheKey);
        if (tradeEntity != null) {
            return tradeEntity;
        }

        Trade trade = tradeDao.queryTradeByTradeId(tradeId);
        if (trade == null) throw new AppException("交易不存在：tradeId=" + tradeId);

        return TradeEntity.builder()
                .tradeId(trade.getTradeId())
                .activityId(trade.getActivityId())
                .tradeType(TradeType.valueOf(trade.getTradeType()))
                .tradePoint(trade.getTradePoint())
                .tradeValue(trade.getTradeValue())
                .tradeDesc(trade.getTradeDesc())
                .build();
    }

    @Override
    public TradeOrderEntity queryTradeOrderByOrderId(String userId, String orderId) {
        try {
            dbRouterStrategy.doRouter(userId);
            TradeOrder tradeOrder = tradeOrderDao.queryTradeOrderByOrderId(orderId);
            return TradeOrderEntity.builder()
                        .orderId(tradeOrder.getOrderId())
                        .bizId(tradeOrder.getBizId())
                        .userId(tradeOrder.getUserId())
                        .tradeId(tradeOrder.getTradeId())
                        .activityId(tradeOrder.getActivityId())
                        .tradeType(TradeType.valueOf(tradeOrder.getTradeType()))
                        .tradeState(TradeState.valueOf(tradeOrder.getTradeState()))
                        .tradeTime(tradeOrder.getTradeTime())
                        .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public ActivityAccountEntity queryActivityAccount(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccount activityAccount = new ActivityAccount();
            activityAccount.setUserId(userId);
            activityAccount.setActivityId(activityId);
            activityAccount = activityAccountDao.queryActivityAccount(activityAccount);
            if (activityAccount == null) return null;
            return ActivityAccountEntity.builder()
                    .userId(activityAccount.getUserId())
                    .activityId(activityAccount.getActivityId())
                    .accountPoint(activityAccount.getAccountPoint())
                    .totalAllocate(activityAccount.getTotalAllocate())
                    .totalSurplus(activityAccount.getTotalSurplus())
                    .dayLimit(activityAccount.getDayLimit())
                    .monthLimit(activityAccount.getMonthLimit())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void savePointTradeOrder(TaskEntity taskEntity, TradeOrderEntity tradeOrderEntity) {

        String userId = tradeOrderEntity.getUserId();
        String orderId = tradeOrderEntity.getOrderId();

        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setOrderId(tradeOrderEntity.getOrderId());
        tradeOrder.setBizId(tradeOrderEntity.getBizId());
        tradeOrder.setUserId(tradeOrderEntity.getUserId());
        tradeOrder.setTradeId(tradeOrderEntity.getTradeId());
        tradeOrder.setActivityId(tradeOrderEntity.getActivityId());
        tradeOrder.setTradeType(tradeOrderEntity.getTradeType().name());
        tradeOrder.setTradeState(tradeOrderEntity.getTradeState().name());
        tradeOrder.setTradeTime(tradeOrderEntity.getTradeTime());

        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState().name());

        try {
            dbRouterStrategy.doRouter(userId);

            transactionTemplate.executeWithoutResult(status -> {
                try {
                    tradeOrderDao.saveTradeOrder(tradeOrder);
                    taskDao.saveTask(task);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【交易】保存交易订单失败：orderId={}", orderId);
                    throw e;
                }
            });

            log.info("【交易】保存交易订单成功：orderId={}", orderId);

            try {
                eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                task.setTaskState(TaskState.DISTRIBUTED.name());
                taskDao.updateTaskState(task);
                log.info("【交易】发送交易消息成功：messageId={}", taskEntity.getMessageId());
            } catch (Exception e) {
                task.setTaskState(TaskState.FAILED.name());
                taskDao.updateTaskState(task);
                log.error("【交易】发送交易消息失败：messageId={}", taskEntity.getMessageId());
                throw e;
            }

        } finally {
            dbRouterStrategy.clear();
        }

    }

    @Override
    public void saveConvertAward(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity) {
        String userId = tradeOrderEntity.getUserId();
        String orderId = tradeOrderEntity.getOrderId();
        Long activityId = tradeOrderEntity.getActivityId();
        Long tradeId = tradeEntity.getTradeId();

        Long awardId = Long.parseLong(tradeEntity.getTradeValue());
        Award award = awardDao.queryAwardByAwardId(awardId);
        if (award == null) {
            throw new AppException("Award 不存在：awardId=" + awardId);
        }

        long seconds = Long.parseLong(award.getAwardValue());
        LocalDateTime deadline = LocalDateTime.now().plusSeconds(seconds);

        int tradePoint = tradeEntity.getTradePoint();
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(activityAccountEntity.getUserId());
        activityAccount.setActivityId(activityAccountEntity.getActivityId());
        activityAccount.setAccountPoint(tradePoint);

        UserAward userAward = new UserAward();
        userAward.setOrderId(orderId);
        userAward.setUserId(userId);
        userAward.setAwardId(awardId);
        userAward.setActivityId(activityId);
        userAward.setAwardSource(AwardSource.CONVERT.name());
        userAward.setAwardName(award.getAwardName());
        userAward.setAwardDesc(award.getAwardDesc());
        userAward.setAwardDeadline(deadline);
        userAward.setAwardTime(LocalDateTime.now());

        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setOrderId(tradeOrderEntity.getOrderId());
        tradeOrder.setBizId(tradeOrderEntity.getBizId());
        tradeOrder.setUserId(tradeOrderEntity.getUserId());
        tradeOrder.setTradeId(tradeOrderEntity.getTradeId());
        tradeOrder.setActivityId(tradeOrderEntity.getActivityId());
        tradeOrder.setTradeType(tradeOrderEntity.getTradeType().name());
        tradeOrder.setTradeState(tradeOrderEntity.getTradeState().name());
        tradeOrder.setTradeTime(tradeOrderEntity.getTradeTime());

        try {
            dbRouterStrategy.doRouter(userId);

            Integer before = activityAccountDao.queryActivityAccountPoint(activityAccount);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    activityAccountDao.decreaseActivityAccountPoint(activityAccount);
                    userAwardDao.saveUserAward(userAward);
                    tradeOrder.setTradeState(TradeState.USED.name());
                    tradeOrderDao.updateTradeState(tradeOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】兑换奖品时发生错误：error={}", e.getMessage());
                    return false;
                }
            });
            Integer after = activityAccountDao.queryActivityAccountPoint(activityAccount);

            if (Boolean.TRUE.equals(success)) {
                log.info("【兑换】兑换奖品成功：orderId={}, userId={}, tradeId={}, awardId={}, point:{}->{}", orderId, userId, tradeId, awardId, before, after);
            } else {
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【兑换】兑换奖品失败：orderId={}, userId={}, tradeId={}, awardId={}, point:{}->{}", orderId, userId, tradeId, awardId, before, after);
            }

        } finally {
            dbRouterStrategy.clear();
        }

    }

    @Override
    public void saveConvertRaffle(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity) {

        String userId = tradeOrderEntity.getUserId();
        String orderId = tradeOrderEntity.getOrderId();
        Long activityId = tradeOrderEntity.getActivityId();
        Long tradeId = tradeEntity.getTradeId();

        int count = Integer.parseInt(tradeEntity.getTradeValue());
        int tradePoint = tradeEntity.getTradePoint();

        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(activityAccountEntity.getUserId());
        activityAccount.setActivityId(activityAccountEntity.getActivityId());
        activityAccount.setAccountPoint(tradePoint);
        activityAccount.setTotalAllocate(count);
        activityAccount.setTotalSurplus(count);

        String monthKey = TimeUtil.thisMonth(true);
        ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
        activityAccountMonth.setUserId(userId);
        activityAccountMonth.setActivityId(activityId);
        activityAccountMonth.setMonthKey(monthKey);

        String dayKey = TimeUtil.thisDay(true);
        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setUserId(userId);
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setDayKey(dayKey);

        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setOrderId(tradeOrderEntity.getOrderId());
        tradeOrder.setBizId(tradeOrderEntity.getBizId());
        tradeOrder.setUserId(tradeOrderEntity.getUserId());
        tradeOrder.setTradeId(tradeOrderEntity.getTradeId());
        tradeOrder.setActivityId(tradeOrderEntity.getActivityId());
        tradeOrder.setTradeType(tradeOrderEntity.getTradeType().name());
        tradeOrder.setTradeState(tradeOrderEntity.getTradeState().name());
        tradeOrder.setTradeTime(tradeOrderEntity.getTradeTime());

        try {
            dbRouterStrategy.doRouter(userId);

            AccountSnapshot before = getAccountSnapshot(userId, activityId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    activityAccountDao.decreaseActivityAccountPoint(activityAccount);
                    activityAccountDao.increaseActivityAccountRaffle(activityAccount);

                    int monthDelta = Math.min(count, before.getMonthLimit() - before.getMonthAllocate());
                    activityAccountMonth.setMonthAllocate(monthDelta);
                    activityAccountMonth.setMonthSurplus(monthDelta);
                    activityAccountMonthDao.increaseActivityAccountMonthRaffle(activityAccountMonth);

                    int dayDelta = Math.min(count, before.getDayLimit() - before.getDayAllocate());
                    activityAccountDay.setDayAllocate(dayDelta);
                    activityAccountDay.setDaySurplus(dayDelta);
                    activityAccountDayDao.increaseActivityAccountDay(activityAccountDay);

                    tradeOrder.setTradeState(TradeState.USED.name());
                    tradeOrderDao.updateTradeState(tradeOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】兑换抽奖次数发生错误：orderId={}, userId={}, tradeId={}, count={}", orderId, userId, tradeId, count, e);
                    return false;
                }
            });
            AccountSnapshot after = getAccountSnapshot(userId, activityId);

            if (Boolean.TRUE.equals(success)) {
                log.info("【兑换】兑换抽奖次数成功：orderId={}, userId={}, tradeId={}, total:{}->{}, month({}):{}->{}, day({}):{}->{}",
                        orderId, userId, tradeId,
                        before.getTotalSurplus(), after.getTotalSurplus(),
                        monthKey, before.getMonthSurplus(), after.getMonthSurplus(),
                        dayKey, before.getDaySurplus(), after.getDaySurplus()
                );
            } else {
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【兑换】兑换抽奖次数失败：orderId={}, userId={}, tradeId={}, total:{}->{}, month({}):{}->{}, day({}):{}->{}",
                        orderId, userId, tradeId,
                        before.getTotalSurplus(), after.getTotalSurplus(),
                        monthKey, before.getMonthSurplus(), after.getMonthSurplus(),
                        dayKey, before.getDaySurplus(), after.getDaySurplus()
                );
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void savePointRecharge(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity) {

        String userId = tradeOrderEntity.getUserId();
        String orderId = tradeOrderEntity.getOrderId();
        Long tradeId = tradeEntity.getTradeId();

        int rechargePoint = Integer.parseInt(tradeEntity.getTradeValue());

        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(activityAccountEntity.getUserId());
        activityAccount.setActivityId(activityAccountEntity.getActivityId());
        activityAccount.setAccountPoint(rechargePoint);

        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setOrderId(tradeOrderEntity.getOrderId());
        tradeOrder.setBizId(tradeOrderEntity.getBizId());
        tradeOrder.setUserId(tradeOrderEntity.getUserId());
        tradeOrder.setTradeId(tradeOrderEntity.getTradeId());
        tradeOrder.setActivityId(tradeOrderEntity.getActivityId());
        tradeOrder.setTradeType(tradeOrderEntity.getTradeType().name());
        tradeOrder.setTradeState(tradeOrderEntity.getTradeState().name());
        tradeOrder.setTradeTime(tradeOrderEntity.getTradeTime());

        try {
            dbRouterStrategy.doRouter(userId);

            Integer before = activityAccountDao.queryActivityAccountPoint(activityAccount);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    activityAccountDao.increaseActivityAccountPoint(activityAccount);
                    tradeOrder.setTradeState(TradeState.USED.name());
                    tradeOrderDao.updateTradeState(tradeOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】充值积分发生错误：orderId={}, userId={}, tradeId={}, point={}", orderId, userId, tradeId, rechargePoint, e);
                    return false;
                }
            });
            Integer after = activityAccountDao.queryActivityAccountPoint(activityAccount);

            if (Boolean.TRUE.equals(success)) {
                log.info("【充值】充值积分成功：orderId={}, userId={}, tradeId={}, point:{}->{}", orderId, userId, tradeId, before, after);
            } else {
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【充值】充值积分失败：orderId={}, userId={}, tradeId={}, point:{}->{}", orderId, userId, tradeId, before, after);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }


    private AccountSnapshot getAccountSnapshot(String userId, Long activityId) {
        // ===== 1. 总账户 =====
        ActivityAccount accountReq = new ActivityAccount();
        accountReq.setUserId(userId);
        accountReq.setActivityId(activityId);
        ActivityAccount account = activityAccountDao.queryActivityAccount(accountReq);

        // ===== 2. 月账户 =====
        String monthKey = TimeUtil.thisMonth(true);
        ActivityAccountMonth monthReq = new ActivityAccountMonth();
        monthReq.setUserId(userId);
        monthReq.setActivityId(activityId);
        monthReq.setMonthKey(monthKey);
        ActivityAccountMonth monthAccount = activityAccountMonthDao.queryActivityAccountMonth(monthReq);

        // ===== 3. 日账户 =====
        String dayKey = TimeUtil.thisDay(true);
        ActivityAccountDay dayReq = new ActivityAccountDay();
        dayReq.setUserId(userId);
        dayReq.setActivityId(activityId);
        dayReq.setDayKey(dayKey);
        ActivityAccountDay dayAccount = activityAccountDayDao.queryActivityAccountDay(dayReq);

        // ===== 4. 组装快照 =====
        return AccountSnapshot.builder()
                .totalAllocate(account == null ? 0 : account.getTotalAllocate())
                .totalSurplus(account == null ? 0 : account.getTotalSurplus())
                .monthLimit(monthAccount == null ? 0 : monthAccount.getMonthLimit())
                .monthAllocate(monthAccount == null ? 0 : monthAccount.getMonthAllocate())
                .monthSurplus(monthAccount == null ? 0 : monthAccount.getMonthSurplus())
                .dayLimit(dayAccount == null ? 0 : dayAccount.getDayLimit())
                .dayAllocate(dayAccount == null ? 0 : dayAccount.getDayAllocate())
                .daySurplus(dayAccount == null ? 0 : dayAccount.getDaySurplus())
                .build();
    }


}

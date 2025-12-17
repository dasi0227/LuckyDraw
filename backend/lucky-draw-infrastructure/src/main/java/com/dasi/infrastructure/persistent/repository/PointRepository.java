package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.point.model.entity.ActivityAccountEntity;
import com.dasi.domain.point.model.entity.TaskEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.type.*;
import com.dasi.domain.point.model.vo.AccountSnapshot;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import com.dasi.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

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
    private IRewardOrderDao rewardOrderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IRedisService redisService;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private EventPublisher eventPublisher;

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
                        .tradeMoney(trade.getTradeMoney())
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
                .tradeMoney(trade.getTradeMoney())
                .tradeValue(trade.getTradeValue())
                .tradeName(trade.getTradeName())
                .tradeDesc(trade.getTradeDesc())
                .build();
    }

    @Override
    public List<TradeEntity> queryActivityRechargeList(Long activityId) {
        List<Trade> tradeList = tradeDao.queryActivityRecharegeList(activityId);
        return tradeList.stream()
                .map(trade -> TradeEntity.builder()
                        .tradeId(trade.getTradeId())
                        .activityId(trade.getActivityId())
                        .tradeType(TradeType.valueOf(trade.getTradeType()))
                        .tradePoint(trade.getTradePoint())
                        .tradeMoney(trade.getTradeMoney())
                        .tradeValue(trade.getTradeValue())
                        .tradeName(trade.getTradeName())
                        .tradeDesc(trade.getTradeDesc())
                        .build())
                .collect(Collectors.toList());
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
    public void saveRechargePoint(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity) {

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

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    tradeOrder.setTradeState(TradeState.USED.name());
                    int rows = tradeOrderDao.updateTradeState(tradeOrder);
                    if (rows == 1) {
                        activityAccountDao.increaseActivityAccountPoint(activityAccount);
                    }
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】交易充值积分时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【充值】交易充值积分成功：orderId={}, userId={}, tradeId={}, point={}", orderId, userId, tradeId, rechargePoint);
            } else {
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【充值】交易充值积分失败：orderId={}, userId={}, tradeId={}, point={}", orderId, userId, tradeId, rechargePoint);
            }
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
    public void savePointTradeOrder(ActivityAccountEntity activityAccountEntity, TaskEntity taskEntity, TradeOrderEntity tradeOrderEntity) {

        String userId = tradeOrderEntity.getUserId();
        String orderId = tradeOrderEntity.getOrderId();

        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(activityAccountEntity.getUserId());
        activityAccount.setActivityId(activityAccountEntity.getActivityId());
        activityAccount.setAccountPoint(activityAccountEntity.getAccountPoint());

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

            AtomicInteger atomicRows = new AtomicInteger(0);
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    int rows = tradeOrderDao.saveTradeOrder(tradeOrder);
                    if (rows == 1) {
                        atomicRows.set(1);
                        taskDao.saveTask(task);
                        activityAccountDao.decreaseActivityAccountPoint(activityAccount);
                    }
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【交易】保存交易订单失败：orderId={}", orderId);
                    throw e;
                }
            });

            if (atomicRows.get() == 0) {
                return;
            }

            try {
                eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                task.setTaskState(TaskState.DISTRIBUTED.name());
                taskDao.updateTaskState(task);
                log.info("【交易】保存交易订单成功：orderId={}", orderId);
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

        UserAward userAward = new UserAward();
        userAward.setOrderId(orderId);
        userAward.setUserId(userId);
        userAward.setAwardId(awardId);
        userAward.setActivityId(activityId);
        userAward.setAwardSource(AwardSource.CONVERT.name());
        userAward.setAwardName(award.getAwardName());
        userAward.setAwardDesc(award.getAwardDesc());
        userAward.setAwardDeadline(null);
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

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    tradeOrder.setTradeState(TradeState.USED.name());
                    int rows = tradeOrderDao.updateTradeState(tradeOrder);
                    if (rows == 1) {
                        userAwardDao.saveUserAward(userAward);
                    }
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】兑换奖品时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【兑换】兑换奖品成功：orderId={}, userId={}, tradeId={}, awardId={}", orderId, userId, tradeId, awardId);
            } else {
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【兑换】兑换奖品失败：orderId={}, userId={}, tradeId={}, awardId={}", orderId, userId, tradeId, awardId);
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

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    tradeOrder.setTradeState(TradeState.USED.name());
                    int rows = tradeOrderDao.updateTradeState(tradeOrder);

                    if (rows == 1) {
                        AccountSnapshot accountSnapshot = getAccountSnapshot(userId, activityId);

                        activityAccountDao.increaseActivityAccountRaffle(activityAccount);

                        int monthDelta = Math.min(count, accountSnapshot.getMonthLimit() - accountSnapshot.getMonthAllocate());
                        activityAccountMonth.setMonthAllocate(monthDelta);
                        activityAccountMonth.setMonthSurplus(monthDelta);
                        activityAccountMonthDao.increaseActivityAccountMonthRaffle(activityAccountMonth);

                        int dayDelta = Math.min(count, accountSnapshot.getDayLimit() - accountSnapshot.getDayAllocate());
                        activityAccountDay.setDayAllocate(dayDelta);
                        activityAccountDay.setDaySurplus(dayDelta);
                        activityAccountDayDao.increaseActivityAccountDay(activityAccountDay);
                    }
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】兑换抽奖次数发生错误：orderId={}, userId={}, tradeId={}, count={}", orderId, userId, tradeId, count, e);
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【兑换】兑换抽奖次数成功：orderId={}, userId={}, tradeId={}, count={}", orderId, userId, tradeId, count);
            } else {
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【兑换】兑换抽奖次数失败：orderId={}, userId={}, tradeId={}, count={}", orderId, userId, tradeId, count);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void saveRewardPoint(ActivityAccountEntity activityAccountEntity, TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity) {

        String userId = tradeOrderEntity.getUserId();
        String orderId = tradeOrderEntity.getOrderId();
        Long tradeId = tradeEntity.getTradeId();
        String bizId = tradeOrderEntity.getBizId();
        int rechargePoint = Integer.parseInt(tradeEntity.getTradeValue());

        RewardOrder rewardOrder = new RewardOrder();
        rewardOrder.setBizId(bizId);

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

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    rewardOrder.setRewardState(RewardState.USED.name());
                    int rewardRows = rewardOrderDao.updateRewardOrderStateByBizId(rewardOrder);
                    if (rewardRows == 1){
                        tradeOrder.setTradeState(TradeState.USED.name());
                        int tradeRows = tradeOrderDao.updateTradeState(tradeOrder);
                        if (tradeRows == 1) {
                            activityAccountDao.increaseActivityAccountPoint(activityAccount);
                        }
                    }

                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】充值积分发生错误：orderId={}, userId={}, tradeId={}, point={}", orderId, userId, tradeId, rechargePoint, e);
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【充值】充值积分成功：orderId={}, userId={}, tradeId={}, point={}", orderId, userId, tradeId, rechargePoint);
            } else {
                rewardOrder.setRewardState(RewardState.CANCELLED.name());
                rewardOrderDao.updateRewardOrderStateByBizId(rewardOrder);
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【充值】充值积分失败：orderId={}, userId={}, tradeId={}, point={}", orderId, userId, tradeId, rechargePoint);
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

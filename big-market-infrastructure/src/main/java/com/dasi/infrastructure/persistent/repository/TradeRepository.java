package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.trade.model.entity.TaskEntity;
import com.dasi.domain.trade.model.entity.TradeEntity;
import com.dasi.domain.trade.model.entity.TradeOrderEntity;
import com.dasi.domain.trade.model.type.TaskState;
import com.dasi.domain.trade.model.type.TradeState;
import com.dasi.domain.trade.model.type.TradeType;
import com.dasi.domain.trade.model.vo.AccountSnapshot;
import com.dasi.domain.trade.repository.ITradeRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.DefaultValue;
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
public class TradeRepository implements ITradeRepository {

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
    public List<TradeEntity> queryConvertListByActivityId(Long activityId) {
        String cacheKey = RedisKey.TRADE_LIST_KEY + activityId;
        List<TradeEntity> tradeEntityList = redisService.getValue(cacheKey);
        if (tradeEntityList != null && !tradeEntityList.isEmpty()) {
            return tradeEntityList;
        }

        List<Trade> tradeList = tradeDao.queryConvertListByActivityId(activityId);
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
    public void createActivityAccountIfAbsent(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);
            ActivityAccount activityAccountReq = new ActivityAccount();
            activityAccountReq.setUserId(userId);
            activityAccountReq.setActivityId(activityId);
            ActivityAccount activityAccount = activityAccountDao.queryActivityAccount(activityAccountReq);
            if (activityAccount == null) {
                activityAccount = new ActivityAccount();
                activityAccount.setUserId(userId);
                activityAccount.setActivityId(activityId);
                activityAccount.setActivityPoint(0);
                activityAccount.setTotalAllocate(0);
                activityAccount.setTotalSurplus(0);
                activityAccount.setMonthLimit(DefaultValue.MONTH_LIMIT);
                activityAccount.setDayLimit(DefaultValue.DAY_LIMIT);
                activityAccountDao.createActivityAccount(activityAccount);
            }
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void savePointRechargeTradeOrder(TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity) {

        String userId = tradeOrderEntity.getUserId();
        String orderId = tradeOrderEntity.getOrderId();
        Long activityId = tradeOrderEntity.getActivityId();

        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setOrderId(tradeOrderEntity.getOrderId());
        tradeOrder.setBizId(tradeOrderEntity.getBizId());
        tradeOrder.setUserId(tradeOrderEntity.getUserId());
        tradeOrder.setTradeId(tradeOrderEntity.getTradeId());
        tradeOrder.setActivityId(tradeOrderEntity.getActivityId());
        tradeOrder.setTradeType(tradeOrderEntity.getTradeType().name());
        tradeOrder.setTradeState(tradeOrderEntity.getTradeState().name());
        tradeOrder.setTradeTime(tradeOrderEntity.getTradeTime());

        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(userId);
        activityAccount.setActivityId(activityId);
        activityAccount.setActivityPoint(Integer.valueOf(tradeEntity.getTradeValue()));

        try {
            dbRouterStrategy.doRouter(userId);

            Integer before = activityAccountDao.queryActivityAccountPoint(activityAccount);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 执行交易
                    activityAccountDao.increaseActivityAccountPoint(activityAccount);
                    // 写入订单
                    tradeOrderDao.saveTradeOrder(tradeOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】保存充值订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });
            Integer after = activityAccountDao.queryActivityAccountPoint(activityAccount);

            if (Boolean.TRUE.equals(success)) {
                tradeOrder.setTradeState(TradeState.USED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                log.info("【充值】增加账户积分成功：userId={}, point:{}->{}", userId, before, after);
                log.info("【充值】保存充值订单成功：orderId={}", orderId);
            } else {
                tradeOrder.setTradeState(TradeState.CANCELLED.name());
                tradeOrderDao.updateTradeState(tradeOrder);
                throw new AppException("保存充值订单失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }

    }

    @Override
    public void savePointConvertTradeOrder(TaskEntity taskEntity, TradeOrderEntity tradeOrderEntity) {

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

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 写入记录
                    tradeOrderDao.saveTradeOrder(tradeOrder);
                    taskDao.saveTask(task);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】保存兑换订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【兑换】保存兑换订单成功：orderId={}", orderId);
                try {
                    eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                    task.setTaskState(TaskState.DISTRIBUTED.name());
                    taskDao.updateTaskState(task);
                    log.info("【兑换】发送兑换消息成功：messageId={}", taskEntity.getMessageId());
                } catch (Exception e) {
                    task.setTaskState(TaskState.FAILED.name());
                    taskDao.updateTaskState(task);
                    throw new AppException("发送兑换消息失败：messageId=" + taskEntity.getMessageId());
                }
            } else {
                task.setTaskState(TaskState.FAILED.name());
                taskDao.updateTaskState(task);
                throw new AppException("发送兑换消息失败：messageId=" + taskEntity.getMessageId());
            }

        } finally {
            dbRouterStrategy.clear();
        }

    }

    @Override
    public void updateTradeOrderState(TradeOrderEntity tradeOrderEntity) {
        try {
            dbRouterStrategy.doRouter(tradeOrderEntity.getUserId());
            TradeOrder tradeOrder = new TradeOrder();
            tradeOrder.setOrderId(tradeOrderEntity.getOrderId());
            tradeOrder.setTradeState(tradeOrderEntity.getTradeState().name());
            tradeOrderDao.updateTradeState(tradeOrder);
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void doConvertAward(String userId, String orderId, TradeEntity tradeEntity) {

        Long activityId = tradeEntity.getActivityId();
        Long tradeId = tradeEntity.getTradeId();
        Long awardId = Long.parseLong(tradeEntity.getTradeValue());

        Award award = awardDao.queryAwardByAwardId(awardId);
        long seconds = Long.parseLong(award.getAwardValue());
        LocalDateTime awardDeadline = LocalDateTime.now().plusSeconds(seconds);

        Integer tradePoint = tradeEntity.getTradePoint();
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(userId);
        activityAccount.setActivityId(activityId);
        activityAccount.setActivityPoint(tradePoint);

        UserAward userAward = new UserAward();
        userAward.setOrderId(orderId);
        userAward.setUserId(userId);
        userAward.setAwardId(award.getAwardId());
        userAward.setAwardType(award.getAwardType());
        userAward.setAwardName(award.getAwardName());
        userAward.setAwardDesc(award.getAwardDesc());
        userAward.setAwardDeadline(awardDeadline);
        userAward.setAwardTime(LocalDateTime.now());

        try {
            dbRouterStrategy.doRouter(userId);

            Integer before = activityAccountDao.queryActivityAccountPoint(activityAccount);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    activityAccountDao.decreaseActivityAccountPoint(activityAccount);
                    userAwardDao.saveUserAward(userAward);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】兑换奖品时出错：error={}", e.getMessage());
                    return false;
                }
            });
            Integer after = activityAccountDao.queryActivityAccountPoint(activityAccount);

            if (Boolean.TRUE.equals(success)) {
                log.info("【兑换】兑换奖品成功：userId={}, tradeId={}, awardId={}, point={}->{}", userId, tradeId, awardId, before, after);
            } else {
                throw new AppException("兑换奖品失败：userId=" + userId + ", tradeId=" + tradeId);
            }
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void doConvertRaffle(String userId, TradeEntity tradeEntity) {

        Long tradeId = tradeEntity.getTradeId();
        Long activityId = tradeEntity.getActivityId();
        int count = Integer.parseInt(tradeEntity.getTradeValue());

        Integer tradePoint = tradeEntity.getTradePoint();
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(userId);
        activityAccount.setActivityId(activityId);
        activityAccount.setActivityPoint(tradePoint);
        activityAccount.setTotalAllocate(count);
        activityAccount.setTotalSurplus(count);

        String monthKey = TimeUtil.thisMonth(true);
        ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
        activityAccountMonth.setActivityId(activityId);
        activityAccountMonth.setUserId(userId);
        activityAccountMonth.setMonthKey(monthKey);

        String dayKey = TimeUtil.thisDay(true);
        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setUserId(userId);
        activityAccountDay.setDayKey(dayKey);

        try {
            dbRouterStrategy.doRouter(userId);

            AccountSnapshot before = getAccountSnapshot(userId, activityId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 扣减账户的积分
                    activityAccountDao.decreaseActivityAccountPoint(activityAccount);

                    // 执行总账户的充值
                    activityAccountDao.increaseActivityAccountRaffle(activityAccount);

                    // 执行月账户的充值
                    int monthDelta = Math.min(count, before.getMonthLimit() - before.getMonthAllocate());
                    activityAccountMonth.setMonthAllocate(monthDelta);
                    activityAccountMonth.setMonthSurplus(monthDelta);
                    activityAccountMonthDao.increaseActivityAccountMonthRaffle(activityAccountMonth);

                    // 执行日账户的充值
                    int dayDelta   = Math.min(count, before.getDayLimit() - before.getDayAllocate());
                    activityAccountDay.setDayAllocate(dayDelta);
                    activityAccountDay.setDaySurplus(dayDelta);
                    activityAccountDayDao.increaseActivityAccountDay(activityAccountDay);

                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】兑换抽奖次数时出错：error={}", e.getMessage());
                    return false;
                }
            });
            AccountSnapshot after = getAccountSnapshot(userId, activityId);

            if (Boolean.TRUE.equals(success)) {
                log.info("【兑换】兑换抽奖次数成功：userId={}, activityId={}, total:{}->{}, month({}):{}->{}, day({}):{}->{}",
                        userId, activityId,
                        before.getTotalSurplus(),  after.getTotalSurplus(),
                        monthKey,   before.getMonthSurplus(),  after.getMonthSurplus(),
                        dayKey,     before.getDaySurplus(),    after.getDaySurplus()
                );
            } else {
                throw new AppException("兑换抽奖次数失败：userId=" + userId + ", tradeId=" + tradeId);
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

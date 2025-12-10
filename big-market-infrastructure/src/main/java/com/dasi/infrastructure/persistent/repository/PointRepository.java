package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.point.model.entity.TaskEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.type.TaskState;
import com.dasi.domain.point.model.type.TradeState;
import com.dasi.domain.point.model.type.TradeType;
import com.dasi.domain.point.model.type.UserState;
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
    private IUserAccountDao userAccountDao;

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
    public Integer queryUserPointByUserId(String userId) {
        try {
            dbRouterStrategy.doRouter(userId);
            return userAccountDao.queryUserPointByUserId(userId);
        } finally {
            dbRouterStrategy.clear();
        }
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
    public void createUserAccountIfAbsent(String userId) {
        try {
            dbRouterStrategy.doRouter(userId);
            UserAccount userAccount = userAccountDao.queryUserAccountByUserId(userId);
            if (userAccount == null) {
                userAccount = new UserAccount();
                userAccount.setUserId(userId);
                userAccount.setUserState(UserState.ENABLE.name());
                userAccount.setUserPoint(0);
                userAccountDao.createUserAccount(userAccount);
            }
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void savePointRechargeTradeOrder(TradeEntity tradeEntity, TradeOrderEntity tradeOrderEntity) {

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

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setUserPoint(Integer.valueOf(tradeEntity.getTradeValue()));

        try {
            dbRouterStrategy.doRouter(userId);

            Integer before = userAccountDao.queryUserPointByUserId(userId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    // 执行交易
                    userAccountDao.increaseUserAccountPoint(userAccount);
                    // 写入订单
                    tradeOrderDao.saveTradeOrder(tradeOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】保存充值订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });
            Integer after = userAccountDao.queryUserPointByUserId(userId);

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

        Long tradeId = tradeEntity.getTradeId();
        Long awardId = Long.parseLong(tradeEntity.getTradeValue());

        Award award = awardDao.queryAwardByAwardId(awardId);
        long seconds = Long.parseLong(award.getAwardValue());
        LocalDateTime awardDeadline = LocalDateTime.now().plusSeconds(seconds);

        Integer tradePoint = Integer.valueOf(tradeEntity.getTradePoint());
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setUserPoint(tradePoint);

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

            Integer before = userAccountDao.queryUserPointByUserId(userId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    userAccountDao.decreaseUserAccountPoint(userAccount);
                    userAwardDao.saveUserAward(userAward);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【兑换】兑换奖品时出错：error={}", e.getMessage());
                    return false;
                }
            });
            Integer after = userAccountDao.queryUserPointByUserId(userId);

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

        Integer tradePoint = Integer.valueOf(tradeEntity.getTradePoint());
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setUserPoint(tradePoint);

        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setActivityId(activityId);
        activityAccount.setUserId(userId);
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
                    userAccountDao.decreaseUserAccountPoint(userAccount);

                    // 执行总账户的充值
                    activityAccountDao.rechargeActivityAccount(activityAccount);

                    // 执行月账户的充值
                    int monthDelta = Math.min(count, before.getMonthLimit() - before.getMonthAllocate());
                    activityAccountMonth.setMonthAllocate(monthDelta);
                    activityAccountMonth.setMonthSurplus(monthDelta);
                    activityAccountMonthDao.rechargeActivityAccountMonth(activityAccountMonth);

                    // 执行日账户的充值
                    int dayDelta   = Math.min(count, before.getDayLimit() - before.getDayAllocate());
                    activityAccountDay.setDayAllocate(dayDelta);
                    activityAccountDay.setDaySurplus(dayDelta);
                    activityAccountDayDao.rechargeActivityAccountDay(activityAccountDay);

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

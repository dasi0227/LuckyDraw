package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.event.RechargeSkuStockEmptyEvent;
import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.model.io.ActivitySkuStock;
import com.dasi.domain.activity.model.type.ActivityState;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.activity.model.type.RechargeState;
import com.dasi.domain.activity.model.type.RewardState;
import com.dasi.domain.activity.model.vo.AccountSnapshot;
import com.dasi.domain.activity.model.vo.ActivitySnapshot;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.DefaultValue;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IRedisService redisService;

    @Resource
    private IActivityDao activityDao;

    @Resource
    private IActivitySkuDao rechargeSkuDao;

    @Resource
    private IRechargeOrderDao rechargeOrderDao;

    @Resource
    private IActivityAccountDao activityAccountDao;

    @Resource
    private IActivityAccountMonthDao activityAccountMonthDao;

    @Resource
    private IActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IActivityAwardDao activityAwardDao;

    @Resource
    private IRaffleOrderDao raffleOrderDao;

    @Resource
    private IRewardOrderDao rewardOrderDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private RechargeSkuStockEmptyEvent rechargeSkuStockEmptyEvent;

    @Override
    public ActivitySkuEntity queryRechargeSkuBySkuId(Long skuId) {
        ActivitySku activitySku = rechargeSkuDao.queryRechargeSkuBySkuId(skuId);
        if (activitySku == null) throw new AppException("ActivitySku 不存在：skuId=" + skuId);
        return ActivitySkuEntity.builder()
                .skuId(activitySku.getSkuId())
                .activityId(activitySku.getActivityId())
                .count(activitySku.getCount())
                .stockAllocate(activitySku.getStockAllocate())
                .stockSurplus(activitySku.getStockSurplus())
                .build();
    }

    @Override
    public List<ActivitySkuEntity> queryRechargeSkuByActivityId(Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_RECHARGE_SKU_KEY + activityId;
        List<ActivitySkuEntity> activitySkuEntityList = redisService.getValue(cacheKey);
        if (activitySkuEntityList != null && !activitySkuEntityList.isEmpty()) {
            return activitySkuEntityList;
        }

        // 再查数据库
        List<ActivitySku> activitySkuList = rechargeSkuDao.queryRechargeSkuByActivityId(activityId);
        if (activitySkuList == null || activitySkuList.isEmpty()) throw new AppException("RechargeSkuList 不存在：activityId=" + activityId);
        activitySkuEntityList = activitySkuList.stream()
                .map(activitySku -> ActivitySkuEntity.builder()
                        .skuId(activitySku.getSkuId())
                        .activityId(activitySku.getActivityId())
                        .count(activitySku.getCount())
                        .stockAllocate(activitySku.getStockAllocate())
                        .stockSurplus(activitySku.getStockSurplus())
                        .build())
                .collect(Collectors.toList());

        // 缓存并返回
        redisService.setValue(cacheKey, activitySkuEntityList);
        return activitySkuEntityList;

    }

    @Override
    public ActivityEntity queryActivityByActivityId(Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_KEY + activityId;
        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        if (activityEntity != null) {
            return activityEntity;
        }

        // 再查数据库
        Activity activity = activityDao.queryActivityByActivityId(activityId);
        if (activity == null) throw new AppException("Activity 不存在：activity=Id" + activityId);
        activityEntity = ActivityEntity.builder()
                .activityId(activity.getActivityId())
                .activityName(activity.getActivityName())
                .activityDesc(activity.getActivityDesc())
                .activityBeginTime(activity.getActivityBeginTime())
                .activityEndTime(activity.getActivityEndTime())
                .strategyId(activity.getStrategyId())
                .activityState(ActivityState.valueOf(activity.getActivityState()))
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityAccountEntity queryActivityAccount(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccount activityAccount = new ActivityAccount();
            activityAccount.setUserId(userId);
            activityAccount.setActivityId(activityId);
            activityAccount = activityAccountDao.queryActivityAccount(activityAccount);
            if (activityAccount == null) return null; // 交给上层处理 null
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
    public ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
            activityAccountMonth.setUserId(userId);
            activityAccountMonth.setActivityId(activityId);
            activityAccountMonth.setMonthKey(month);
            activityAccountMonth = activityAccountMonthDao.queryActivityAccountMonth(activityAccountMonth);
            if (activityAccountMonth == null) return null; // 交给上层处理 null
            return ActivityAccountMonthEntity.builder()
                    .activityId(activityAccountMonth.getActivityId())
                    .userId(activityAccountMonth.getUserId())
                    .monthLimit(activityAccountMonth.getMonthLimit())
                    .monthKey(activityAccountMonth.getMonthKey())
                    .monthAllocate(activityAccountMonth.getMonthAllocate())
                    .monthSurplus(activityAccountMonth.getMonthSurplus())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day) {
        try {
            dbRouterStrategy.doRouter(userId);

            ActivityAccountDay activityAccountDay = new ActivityAccountDay();
            activityAccountDay.setUserId(userId);
            activityAccountDay.setActivityId(activityId);
            activityAccountDay.setDayKey(day);
            activityAccountDay = activityAccountDayDao.queryActivityAccountDay(activityAccountDay);
            if (activityAccountDay == null) return null; // 交给上层处理 null
            return ActivityAccountDayEntity.builder()
                    .activityId(activityAccountDay.getActivityId())
                    .userId(activityAccountDay.getUserId())
                    .dayLimit(activityAccountDay.getDayLimit())
                    .dayKey(activityAccountDay.getDayKey())
                    .dayAllocate(activityAccountDay.getDayAllocate())
                    .daySurplus(activityAccountDay.getDaySurplus())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public RaffleOrderEntity queryUnusedRaffleOrder(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            RaffleOrder raffleOrderReq = new RaffleOrder();
            raffleOrderReq.setUserId(userId);
            raffleOrderReq.setActivityId(activityId);
            RaffleOrder raffleOrder = raffleOrderDao.queryUnusedRaffleOrder(raffleOrderReq);
            if (raffleOrder == null) return null;
            return RaffleOrderEntity.builder()
                    .orderId(raffleOrder.getOrderId())
                    .userId(raffleOrder.getUserId())
                    .activityId(raffleOrder.getActivityId())
                    .strategyId(raffleOrder.getStrategyId())
                    .raffleState(RaffleState.valueOf(raffleOrder.getRaffleState()))
                    .raffleTime(raffleOrder.getRaffleTime())
                    .build();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void cacheRechargeSkuStockSurplus(Long skuId, Integer stockSurplus) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_SURPLUS_KEY + skuId;
        redisService.setAtomicLong(cacheKey, Long.valueOf(stockSurplus));
    }

    // -1 表示为空，-2 表示错误
    @Override
    public Long subtractRechargeSkuStockSurplus(Long skuId, LocalDateTime activityEndTime) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_SURPLUS_KEY + skuId;
        if (!redisService.isExists(cacheKey)) return -2L;

        long surplus = redisService.decr(cacheKey);
        if (surplus < -1L) {
            redisService.setAtomicLong(cacheKey, 0L);
            return -2L;
        }
        if (surplus == -1L) {
            redisService.setAtomicLong(cacheKey, 0L);
            return -1L;
        }
        if (surplus == 0L) {
            eventPublisher.publish(rechargeSkuStockEmptyEvent.getTopic(), rechargeSkuStockEmptyEvent.buildEventMessage(skuId));
            return surplus;
        }

        String lockKey = cacheKey + Delimiter.UNDERSCORE + surplus;
        Duration expire = Duration.ofMillis(
                activityEndTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        - System.currentTimeMillis()
                        + TimeUnit.DAYS.toMillis(1));
        if (!redisService.setNx(lockKey, expire)) {
            return -2L;
        }
        return surplus;
    }

    @Override
    public void sendActivitySkuStockConsumeToMQ(ActivitySkuStock activitySkuStock) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<ActivitySkuStock> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(activitySkuStock, 3, TimeUnit.SECONDS);
    }

    @Override
    public ActivitySkuStock getQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        blockingQueue.clear();
    }

    @Override
    public void updateRechargeSkuStock(Long skuId) {
        rechargeSkuDao.updateRechargeSkuStock(skuId);
    }

    @Override
    public void clearRechargeSkuStock(Long skuId) {
        rechargeSkuDao.clearRechargeSkuStock(skuId);
    }

    @Override
    public Integer increaseActivityAccountLuck(ActivityAccountEntity activityAccountEntity) {

        String userId = activityAccountEntity.getUserId();

        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(activityAccountEntity.getUserId());
        activityAccount.setActivityId(activityAccountEntity.getActivityId());
        activityAccount.setAccountLuck(activityAccountEntity.getAccountLuck());

        try {
            dbRouterStrategy.doRouter(userId);
            activityAccountDao.increaseActivityAccountLuck(activityAccount);
            return activityAccountDao.queryActivityAccountLuck(activityAccount);
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void createActivityAccountIfAbsent(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);

            transactionTemplate.execute(status -> {
                try {
                    /* ========== 1. 查询/创建 总账户 ========== */
                    ActivityAccount accountReq = new ActivityAccount();
                    accountReq.setUserId(userId);
                    accountReq.setActivityId(activityId);
                    ActivityAccount activityAccount = activityAccountDao.queryActivityAccount(accountReq);
                    if (activityAccount == null) {
                        activityAccount = new ActivityAccount();
                        activityAccount.setUserId(userId);
                        activityAccount.setActivityId(activityId);
                        activityAccount.setAccountPoint(0);
                        activityAccount.setAccountLuck(0);
                        activityAccount.setTotalAllocate(0);
                        activityAccount.setTotalSurplus(0);
                        activityAccount.setMonthLimit(DefaultValue.MONTH_LIMIT);
                        activityAccount.setDayLimit(DefaultValue.DAY_LIMIT);
                        activityAccountDao.createActivityAccount(activityAccount);
                    }

                    /* ========== 2. 查询/创建 日账户 ========== */
                    String dayKey = TimeUtil.thisDay(true);
                    Integer dayLimit = activityAccount.getDayLimit();
                    ActivityAccountDay dayReq = new ActivityAccountDay();
                    dayReq.setUserId(userId);
                    dayReq.setActivityId(activityId);
                    dayReq.setDayKey(dayKey);
                    ActivityAccountDay activityAccountDay = activityAccountDayDao.queryActivityAccountDay(dayReq);
                    if (activityAccountDay == null) {
                        activityAccountDay = new ActivityAccountDay();
                        activityAccountDay.setActivityId(activityId);
                        activityAccountDay.setUserId(userId);
                        activityAccountDay.setDayKey(dayKey);
                        activityAccountDay.setDayLimit(dayLimit);
                        activityAccountDay.setDayAllocate(Math.min(dayLimit, activityAccount.getTotalSurplus()));
                        activityAccountDay.setDaySurplus(Math.min(dayLimit, activityAccount.getTotalSurplus()));
                        activityAccountDayDao.createActivityAccountDay(activityAccountDay);
                    }

                    /* ========== 3. 查询/创建 月账户 ========== */
                    String monthKey = TimeUtil.thisMonth(true);
                    Integer monthLimit = activityAccount.getMonthLimit();
                    ActivityAccountMonth monthReq = new ActivityAccountMonth();
                    monthReq.setUserId(userId);
                    monthReq.setActivityId(activityId);
                    monthReq.setMonthKey(monthKey);
                    ActivityAccountMonth activityAccountMonth = activityAccountMonthDao.queryActivityAccountMonth(monthReq);
                    if (activityAccountMonth == null) {
                        activityAccountMonth = new ActivityAccountMonth();
                        activityAccountMonth.setActivityId(activityId);
                        activityAccountMonth.setUserId(userId);
                        activityAccountMonth.setMonthKey(monthKey);
                        activityAccountMonth.setMonthLimit(monthLimit);
                        activityAccountMonth.setMonthAllocate(Math.min(monthLimit, activityAccount.getTotalSurplus()));
                        activityAccountMonth.setMonthSurplus(Math.min(monthLimit, activityAccount.getTotalSurplus()));
                        activityAccountMonthDao.createActivityAccountMonth(activityAccountMonth);
                    }

                    return null;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("创建账户时失败：error={}", e.getMessage());
                    throw new AppException("创建账户失败：userId=" + userId + ", activityId=" + activityId);
                }
            });
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void saveRechargeOrder(RechargeOrderEntity rechargeOrderEntity) {

        // 奖励对象
        RewardOrder rewardOrder = new RewardOrder();
        rewardOrder.setBizId(rechargeOrderEntity.getBizId());

        // 订单对象
        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setOrderId(rechargeOrderEntity.getOrderId());
        rechargeOrder.setBizId(rechargeOrderEntity.getBizId());
        rechargeOrder.setActivityId(rechargeOrderEntity.getActivityId());
        rechargeOrder.setUserId(rechargeOrderEntity.getUserId());
        rechargeOrder.setSkuId(rechargeOrderEntity.getSkuId());
        rechargeOrder.setCount(rechargeOrderEntity.getCount());
        rechargeOrder.setRechargeState(rechargeOrderEntity.getRechargeState().name());
        rechargeOrder.setRechargeTime(rechargeOrderEntity.getRechargeTime());

        // 基础信息
        String orderId = rechargeOrderEntity.getOrderId();
        String userId = rechargeOrderEntity.getUserId();
        Long activityId = rechargeOrderEntity.getActivityId();
        Integer count = rechargeOrderEntity.getCount();

        // 创建用于充值的账户
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

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    rewardOrder.setRewardState(RewardState.USED.name());
                    int rewardRows = rewardOrderDao.updateRewardOrderStateByBizId(rewardOrder);
                    if (rewardRows == 1) {
                        int rechargeRows = rechargeOrderDao.saveRechargeOrder(rechargeOrder);
                        if (rechargeRows == 1) {
                            AccountSnapshot before = getAccountSnapshot(userId, activityId);

                            activityAccountDao.increaseActivityAccountRaffle(activityAccount);

                            int monthDelta = Math.min(count, before.getMonthLimit() - before.getMonthAllocate());
                            activityAccountMonth.setMonthAllocate(monthDelta);
                            activityAccountMonth.setMonthSurplus(monthDelta);
                            activityAccountMonthDao.increaseActivityAccountMonthRaffle(activityAccountMonth);

                            int dayDelta = Math.min(count, before.getDayLimit() - before.getDayAllocate());
                            activityAccountDay.setDayAllocate(dayDelta);
                            activityAccountDay.setDaySurplus(dayDelta);
                            activityAccountDayDao.increaseActivityAccountDay(activityAccountDay);

                            rechargeOrder.setRechargeState(RechargeState.USED.name());
                            rechargeOrderDao.updateRechargeState(rechargeOrder);
                        }
                    }
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】保存充值订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【充值】增加账户抽奖次数成功：userId={}, activityId={}, count={}", userId, activityId, count);
                log.info("【充值】保存充值订单成功：orderId={}", orderId);
            } else {
                rechargeOrder.setRechargeState(RechargeState.CANCELLED.name());
                rechargeOrderDao.updateRechargeState(rechargeOrder);
                rewardOrder.setRewardState(RewardState.CANCELLED.name());
                rewardOrderDao.updateRewardOrderStateByBizId(rewardOrder);
                throw new AppException("保存充值订单失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void saveRaffleOrder(RaffleOrderEntity raffleOrderEntity) {

        String orderId = raffleOrderEntity.getOrderId();
        String userId = raffleOrderEntity.getUserId();
        Long activityId = raffleOrderEntity.getActivityId();
        String monthKey = TimeUtil.thisMonth(true);
        String dayKey = TimeUtil.thisDay(true);

        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(userId);
        activityAccount.setActivityId(activityId);

        ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
        activityAccountMonth.setUserId(userId);
        activityAccountMonth.setActivityId(activityId);
        activityAccountMonth.setMonthKey(monthKey);

        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setUserId(userId);
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setDayKey(dayKey);

        RaffleOrder raffleOrder = new RaffleOrder();
        raffleOrder.setOrderId(raffleOrderEntity.getOrderId());
        raffleOrder.setUserId(raffleOrderEntity.getUserId());
        raffleOrder.setActivityId(raffleOrderEntity.getActivityId());
        raffleOrder.setStrategyId(raffleOrderEntity.getStrategyId());
        raffleOrder.setRaffleState(raffleOrderEntity.getRaffleState().name());
        raffleOrder.setRaffleTime(raffleOrderEntity.getRaffleTime());

        try {
            dbRouterStrategy.doRouter(userId);

            Boolean success = transactionTemplate.execute(status -> {
                try {
                    int rows = raffleOrderDao.saveRaffleOrder(raffleOrder);
                    if (rows == 1) {
                        activityAccountDao.decreaseActivityAccountRaffle(activityAccount);
                        activityAccountMonthDao.decreaseActivityAccountMonthRaffle(activityAccountMonth);
                        activityAccountDayDao.decreaseActivityAccountDay(activityAccountDay);

                        raffleOrder.setRaffleState(RaffleState.USED.name());
                        raffleOrderDao.updateRaffleOrderState(raffleOrder);
                    }
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【活动】保存抽奖订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            if (Boolean.TRUE.equals(success)) {
                log.info("【活动】消耗账户抽奖次数成功：userId={}, activityId={}, count={}", userId, activityId, 1);
                log.info("【活动】保存抽奖订单成功：orderId={}", orderId);
            } else {
                raffleOrder.setRaffleState(RaffleState.CANCELLED.name());
                raffleOrderDao.updateRaffleOrderState(raffleOrder);
                throw new AppException("保存抽奖订单失败：orderId=" + orderId);
            }

        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public AccountSnapshot queryAccountSnapshot(String userId, Long activityId) {
        try {
            dbRouterStrategy.doRouter(userId);
            return getAccountSnapshot(userId, activityId);
        } finally {
            dbRouterStrategy.clear();
        }
    }


    public AccountSnapshot getAccountSnapshot(String userId, Long activityId) {

        // ===== 1. 总账户 =====
        ActivityAccount accountReq = new ActivityAccount();
        accountReq.setUserId(userId);
        accountReq.setActivityId(activityId);
        ActivityAccount activityAccount = activityAccountDao.queryActivityAccount(accountReq);

        // ===== 2. 月账户 =====
        String monthKey = TimeUtil.thisMonth(true);
        ActivityAccountMonth monthReq = new ActivityAccountMonth();
        monthReq.setUserId(userId);
        monthReq.setActivityId(activityId);
        monthReq.setMonthKey(monthKey);
        ActivityAccountMonth activityAccountMonth = activityAccountMonthDao.queryActivityAccountMonth(monthReq);

        // ===== 3. 日账户 =====
        String dayKey = TimeUtil.thisDay(true);
        ActivityAccountDay dayReq = new ActivityAccountDay();
        dayReq.setUserId(userId);
        dayReq.setActivityId(activityId);
        dayReq.setDayKey(dayKey);
        ActivityAccountDay activityAccountDay = activityAccountDayDao.queryActivityAccountDay(dayReq);

        // ===== 4. 组装快照 =====
        return AccountSnapshot.builder()
                .accountLuck(activityAccount == null ? 0 : activityAccount.getAccountLuck())
                .accountPoint(activityAccount == null ? 0 : activityAccount.getAccountPoint())
                .totalAllocate(activityAccount == null ? 0 : activityAccount.getTotalAllocate())
                .totalSurplus(activityAccount == null ? 0 : activityAccount.getTotalSurplus())
                .monthLimit(activityAccountMonth == null ? 0 : activityAccountMonth.getMonthLimit())
                .monthAllocate(activityAccountMonth == null ? 0 : activityAccountMonth.getMonthAllocate())
                .monthSurplus(activityAccountMonth == null ? 0 : activityAccountMonth.getMonthSurplus())
                .dayLimit(activityAccountDay == null ? 0 : activityAccountDay.getDayLimit())
                .dayAllocate(activityAccountDay == null ? 0 : activityAccountDay.getDayAllocate())
                .daySurplus(activityAccountDay == null ? 0 : activityAccountDay.getDaySurplus())
                .build();
    }

    @Override
    public ActivitySnapshot queryActivitySnapshot(Long activityId) {

        int activityRaffleCount = 0;
        int activityAccountCount = 0;
        int activityAwardCount = 0;

        // 参与信息
        try {
            int dbCount = dbRouterStrategy.dbCount();
            int tbCount = dbRouterStrategy.tbCount();

            for (int dbIdx = 1; dbIdx <= dbCount; dbIdx++) {

                dbRouterStrategy.setDBKey(dbIdx);
                dbRouterStrategy.setTBKey(0);
                activityAccountCount += activityAccountDao.countByActivityId(activityId);

                for (int tbIdx = 0; tbIdx < tbCount; tbIdx++) {
                    dbRouterStrategy.setTBKey(tbIdx);
                    activityRaffleCount += raffleOrderDao.countByActivityId(activityId);
                    activityAwardCount += activityAwardDao.countByActivityId(activityId);
                }
            }

        } finally {
            dbRouterStrategy.clear();
        }

        Activity activity = activityDao.queryActivityByActivityId(activityId);

        return ActivitySnapshot.builder()
                .activityName(activity.getActivityName())
                .activityDesc(activity.getActivityDesc())
                .activityBeginTime(activity.getActivityBeginTime())
                .activityEndTime(activity.getActivityEndTime())
                .activityRaffleCount(activityRaffleCount)
                .activityAccountCount(activityAccountCount)
                .activityAwardCount(activityAwardCount)
                .build();

    }

}

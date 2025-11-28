package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.event.RechargeSkuStockEmptyEvent;
import com.dasi.domain.activity.model.dto.SkuStock;
import com.dasi.domain.activity.model.dto.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IRedisService redisService;

    @Resource
    private IActivityDao activityDao;

    @Resource
    private IRechargeSkuDao rechargeSkuDao;

    @Resource
    private IRechargeQuotaDao rechargeQuotaDao;

    @Resource
    private IRechargeOrderDao rechargeOrderDao;

    @Resource
    private IActivityAccountDao activityAccountDao;

    @Resource
    private IActivityAccountMonthDao activityAccountMonthDao;

    @Resource
    private IActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IRaffleOrderDao raffleOrderDao;

    @Resource
    private IRaffleAwardDao raffleAwardDao;

    @Resource
    private ITaskDao taskDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private RechargeSkuStockEmptyEvent rechargeSkuStockEmptyEvent;

    @Override
    public RechargeSkuEntity queryRechargeSkuBySkuId(Long skuId) {
        // 先查缓存
        String cacheKey = RedisKey.RECHARGE_SKU_KEY + skuId;
        RechargeSkuEntity rechargeSkuEntity = redisService.getValue(cacheKey);
        if (rechargeSkuEntity != null) {
            return rechargeSkuEntity;
        }

        // 再查数据库
        RechargeSku rechargeSku = rechargeSkuDao.queryRechargeSkuBySku(skuId);
        if (rechargeSku == null) throw new AppException("RechargeSku 不存在，skuId = " + skuId);
        rechargeSkuEntity = RechargeSkuEntity.builder()
                .skuId(rechargeSku.getSkuId())
                .activityId(rechargeSku.getActivityId())
                .quotaId(rechargeSku.getQuotaId())
                .stockAllocate(rechargeSku.getStockAllocate())
                .stockSurplus(rechargeSku.getStockSurplus())
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, rechargeSkuEntity);
        return rechargeSkuEntity;
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
        if (activity == null) throw new AppException("Activity 不存在，请检查 " + activityId);
        activityEntity = ActivityEntity.builder()
                .activityId(activity.getActivityId())
                .activityName(activity.getActivityName())
                .activityDesc(activity.getActivityDesc())
                .activityBeginTime(activity.getActivityBeginTime())
                .activityEndTime(activity.getActivityEndTime())
                .strategyId(activity.getStrategyId())
                .activityState(activity.getActivityState())
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public RechargeQuotaEntity queryRechargeQuotaByQuotaId(Long quotaId) {
        // 先查缓存
        String cacheKey = RedisKey.RECHARGE_QUOTA_KEY + quotaId;
        RechargeQuotaEntity rechargeQuotaEntity = redisService.getValue(cacheKey);
        if (rechargeQuotaEntity != null) {
            return rechargeQuotaEntity;
        }

        // 再查数据库
        RechargeQuota rechargeQuota = rechargeQuotaDao.queryRechargeQuotaByQuotaId(quotaId);
        if (rechargeQuota == null) throw new AppException("RechargeQuota 不存在，请检查 " + quotaId);
        rechargeQuotaEntity = RechargeQuotaEntity.builder()
                .quotaId(rechargeQuota.getQuotaId())
                .totalCount(rechargeQuota.getTotalCount())
                .dayCount(rechargeQuota.getDayCount())
                .monthCount(rechargeQuota.getMonthCount())
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, rechargeQuotaEntity);
        return rechargeQuotaEntity;
    }

    @Override
    public ActivityAccountEntity queryActivityAccount(String userId, Long activityId) {
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(userId);
        activityAccount.setActivityId(activityId);
        activityAccount = activityAccountDao.queryActivityAccount(activityAccount);
        return ActivityAccountEntity.builder()
                .userId(activityAccount.getUserId())
                .activityId(activityAccount.getActivityId())
                .totalAllocate(activityAccount.getTotalAllocate())
                .totalSurplus(activityAccount.getTotalSurplus())
                .dayAllocate(activityAccount.getDayAllocate())
                .daySurplus(activityAccount.getDaySurplus())
                .monthAllocate(activityAccount.getMonthAllocate())
                .monthSurplus(activityAccount.getMonthSurplus())
                .build();
    }

    @Override
    public ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month) {
        ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
        activityAccountMonth.setUserId(userId);
        activityAccountMonth.setActivityId(activityId);
        activityAccountMonth.setMonth(month);
        activityAccountMonth = activityAccountMonthDao.queryActivityAccountMonth(activityAccountMonth);
        return ActivityAccountMonthEntity.builder()
                .activityId(activityAccountMonth.getActivityId())
                .userId(activityAccountMonth.getUserId())
                .month(activityAccountMonth.getMonth())
                .monthAllocate(activityAccountMonth.getMonthAllocate())
                .monthSurplus(activityAccountMonth.getMonthSurplus())
                .build();
    }

    @Override
    public ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day) {
        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setUserId(userId);
        activityAccountDay.setActivityId(activityId);
        activityAccountDay.setDay(day);
        activityAccountDay = activityAccountDayDao.queryActivityAccountDay(activityAccountDay);
        return ActivityAccountDayEntity.builder()
                .activityId(activityAccountDay.getActivityId())
                .userId(activityAccountDay.getUserId())
                .day(activityAccountDay.getDay())
                .dayAllocate(activityAccountDay.getDayAllocate())
                .daySurplus(activityAccountDay.getDaySurplus())
                .build();
    }

    @Override
    public RaffleOrderEntity queryUnusedRaffleOrder(String userId, Long activityId) {
        RaffleOrder raffleOrder = new RaffleOrder();
        raffleOrder.setUserId(userId);
        raffleOrder.setActivityId(activityId);
        raffleOrder = raffleOrderDao.queryUnusedRaffleOrder(raffleOrder);
        return RaffleOrderEntity.builder()
                .orderId(raffleOrder.getOrderId())
                .userId(raffleOrder.getUserId())
                .activityId(raffleOrder.getActivityId())
                .strategyId(raffleOrder.getStrategyId())
                .raffleState(raffleOrder.getRaffleState())
                .raffleTime(raffleOrder.getRaffleTime())
                .build();
    }

    @Override
    public void cacheRechargeSkuStockSurplus(Long skuId, Integer stockSurplus) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_SURPLUS_KEY + skuId;
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, Long.valueOf(stockSurplus));
    }

    // -1 表示为空，-2 表示错误
    @Override
    public Long subtractRechargeSkuStockSurplus(Long skuId, LocalDateTime endTime) {
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
            eventPublisher.publish(rechargeSkuStockEmptyEvent.topic(), rechargeSkuStockEmptyEvent.buildEventMessage(skuId));
            return surplus;
        }

        String lockKey = cacheKey + Delimiter.UNDERSCORE + surplus;
        Duration expire = Duration.ofMillis(
                endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        - System.currentTimeMillis()
                        + TimeUnit.DAYS.toMillis(1));
        return redisService.setNx(lockKey, expire) ? surplus : -2L;
    }

    @Override
    public void sendRechargeSkuStockConsumeToMQ(SkuStock skuStock) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<SkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<SkuStock> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(skuStock, 3, TimeUnit.SECONDS);
    }

    @Override
    public SkuStock getQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<SkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<SkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
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
    public void saveRechargeOrder(RechargeOrderEntity rechargeOrderEntity) {
        // 订单对象
        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setOrderId(rechargeOrderEntity.getOrderId());
        rechargeOrder.setBizId(rechargeOrderEntity.getBizId());
        rechargeOrder.setUserId(rechargeOrderEntity.getUserId());
        rechargeOrder.setSkuId(rechargeOrderEntity.getSkuId());
        rechargeOrder.setStrategyId(rechargeOrderEntity.getStrategyId());
        rechargeOrder.setActivityId(rechargeOrderEntity.getActivityId());
        rechargeOrder.setQuotaId(rechargeOrderEntity.getQuotaId());
        rechargeOrder.setTotalCount(rechargeOrderEntity.getTotalCount());
        rechargeOrder.setMonthCount(rechargeOrderEntity.getMonthCount());
        rechargeOrder.setDayCount(rechargeOrderEntity.getDayCount());
        rechargeOrder.setRechargeState(rechargeOrderEntity.getRechargeState());
        rechargeOrder.setRechargeTime(rechargeOrderEntity.getRechargeTime());

        // 账户对象
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(rechargeOrderEntity.getUserId());
        activityAccount.setActivityId(rechargeOrderEntity.getActivityId());
        activityAccount.setTotalAllocate(rechargeOrderEntity.getTotalCount());
        activityAccount.setTotalSurplus(rechargeOrderEntity.getTotalCount());
        activityAccount.setDayAllocate(rechargeOrderEntity.getDayCount());
        activityAccount.setDaySurplus(rechargeOrderEntity.getDayCount());
        activityAccount.setMonthAllocate(rechargeOrderEntity.getMonthCount());
        activityAccount.setMonthSurplus(rechargeOrderEntity.getMonthCount());

        try {
            dbRouter.doRouter(rechargeOrderEntity.getUserId());
            transactionTemplate.execute(status -> {
                try {
                    // 1. 写入订单
                    rechargeOrderDao.insert(rechargeOrder);
                    // 2. 创建/更新账户
                    int count = activityAccountDao.rechargeActivityAccount(activityAccount);
                    if (count == 0) {
                        activityAccountDao.createActivityAccount(activityAccount);
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.warn("【创建充值订单失败】唯一约束冲突：orderId={}, bizId={}, error={}", rechargeOrderEntity.getOrderId(), rechargeOrderEntity.getBizId(), e.getMessage());
                    throw new AppException("创建充值订单失败，orderId=" + rechargeOrderEntity.getOrderId());
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.warn("【创建充值订单失败】未知异常：error={}", e.getMessage());
                    throw new AppException("创建充值订单失败，orderId=" + rechargeOrderEntity.getOrderId());
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public void saveRaffleOrder(RaffleOrderAggregate raffleOrderAggregate) {
        try {
            String userId = raffleOrderAggregate.getUserId();
            RaffleOrderEntity raffleOrderEntity = raffleOrderAggregate.getRaffleOrderEntity();
            ActivityAccountEntity activityAccountEntity = raffleOrderAggregate.getActivityAccountEntity();

            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    // 1. 更新用户的账户额度
                    ActivityAccount activityAccount = new ActivityAccount();
                    activityAccount.setUserId(activityAccountEntity.getUserId());
                    activityAccount.setActivityId(activityAccountEntity.getActivityId());
                    int count = activityAccountDao.subtractActivityAccount(activityAccount);
                    if (count != 1) {
                        status.setRollbackOnly();
                        log.warn("【创建抽奖订单失败】用户的账户额度不足：total = {}, month = {}, day = {}", activityAccountEntity.getTotalSurplus(), activityAccountEntity.getMonthSurplus(), activityAccountEntity.getDaySurplus());
                    }

                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.warn("【创建抽奖订单失败】唯一约束冲突：orderId={}, error={}", raffleOrderEntity.getOrderId(), e.getMessage());
                    throw new AppException("创建抽奖订单失败，orderId=" + raffleOrderEntity.getOrderId());
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.warn("【创建抽奖订单失败】未知异常：error={}", e.getMessage());
                    throw new AppException("创建抽奖订单失败，orderId=" + raffleOrderEntity.getOrderId());
                }
                return 1;
            });

        } finally {
            dbRouter.clear();
        }
    }

}

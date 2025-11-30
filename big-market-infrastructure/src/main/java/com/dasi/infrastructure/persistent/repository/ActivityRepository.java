package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.event.RechargeSkuStockEmptyEvent;
import com.dasi.domain.activity.model.aggregate.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.model.type.RechargeState;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        RechargeSku rechargeSku = rechargeSkuDao.queryRechargeSkuBySkuId(skuId);
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
    public List<RechargeSkuEntity> queryRechargeSkuByActivityId(Long activityId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_RECHARGE_SKU_KEY + activityId;
        List<RechargeSkuEntity> rechargeSkuEntities = redisService.getValue(cacheKey);
        if (rechargeSkuEntities != null && !rechargeSkuEntities.isEmpty()) {
            return rechargeSkuEntities;
        }

        // 再查数据库
        List<RechargeSku> rechargeSkuList = rechargeSkuDao.queryRechargeSkuByActivityId(activityId);
        rechargeSkuEntities = rechargeSkuList.stream()
                .map(rechargeSku -> RechargeSkuEntity.builder()
                        .skuId(rechargeSku.getSkuId())
                        .activityId(rechargeSku.getActivityId())
                        .quotaId(rechargeSku.getQuotaId())
                        .stockAllocate(rechargeSku.getStockAllocate())
                        .stockSurplus(rechargeSku.getStockSurplus())
                        .build())
                .collect(Collectors.toList());

        // 缓存并返回
        redisService.setValue(cacheKey, rechargeSkuEntities);
        return rechargeSkuEntities;

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
        if (activityAccount == null) return null;
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
        if (activityAccountMonth == null) return null;
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
        if (activityAccountDay == null) return null;
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
        if (raffleOrder == null) return null;
        log.info("【抽奖】存在未完成的抽奖：orderId={}, raffle_state={}", raffleOrder.getOrderId(), raffleOrder.getRaffleState());
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
            eventPublisher.publish(rechargeSkuStockEmptyEvent.topic(), rechargeSkuStockEmptyEvent.buildEventMessage(skuId));
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
    public void sendRechargeSkuStockConsumeToMQ(RechargeSkuStockEntity rechargeSkuStockEntity) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<RechargeSkuStockEntity> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<RechargeSkuStockEntity> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(rechargeSkuStockEntity, 3, TimeUnit.SECONDS);
    }

    @Override
    public RechargeSkuStockEntity getQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<RechargeSkuStockEntity> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<RechargeSkuStockEntity> blockingQueue = redisService.getBlockingQueue(cacheKey);
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
            Integer success = transactionTemplate.execute(status -> {
                try {
                    // 1. 保存/更新账户
                    int count = activityAccountDao.rechargeActivityAccount(activityAccount);
                    if (count == 0) {
                        activityAccountDao.createActivityAccount(activityAccount);
                    }
                    log.info("【充值】账户余额变动：userId={}, skuId={}, total+={}, month+={}, day+={}", rechargeOrderEntity.getUserId(), rechargeOrderEntity.getSkuId(), activityAccount.getTotalSurplus(), activityAccount.getMonthSurplus(), activityAccount.getDaySurplus());

                    // 2. 写入订单
                    rechargeOrderDao.saveRechargeOrder(rechargeOrder);
                    log.info("【充值】保存充值订单成功：userId={}, skuId={}, order={}", rechargeOrderEntity.getUserId(), rechargeOrderEntity.getActivityId(), rechargeOrder.getOrderId());

                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("【充值】保存充值订单失败（唯一约束冲突）：orderId={}, bizId={}, error={}", rechargeOrderEntity.getOrderId(), rechargeOrderEntity.getBizId(), e.getMessage());
                    return 0;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】保存充值订单失败（未知异常）：orderId={}, bizId={}, error={}", rechargeOrderEntity.getOrderId(), rechargeOrderEntity.getBizId(), e.getMessage());
                    return 0;
                }
            });

            if (success != null && success.equals(0)) {
                rechargeOrder.setRechargeState(RechargeState.CANCELLED.getCode());
                rechargeOrderDao.updateRechargeState(rechargeOrder);
                throw new AppException("保存充值订单失败：orderId=" + rechargeOrder.getOrderId());
            } else {
                rechargeOrder.setRechargeState(RechargeState.USED.getCode());
                rechargeOrderDao.updateRechargeState(rechargeOrder);
            }

        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public void saveRaffleOrder(RaffleOrderAggregate raffleOrderAggregate) {
        try {
            String userId = raffleOrderAggregate.getUserId();
            Long activityId = raffleOrderAggregate.getActivityId();
            RaffleOrderEntity raffleOrderEntity = raffleOrderAggregate.getRaffleOrderEntity();
            ActivityAccountEntity activityAccountEntity = raffleOrderAggregate.getActivityAccountEntity();
            ActivityAccountMonthEntity activityAccountMonthEntity = raffleOrderAggregate.getActivityAccountMonthEntity();
            ActivityAccountDayEntity activityAccountDayEntity = raffleOrderAggregate.getActivityAccountDayEntity();

            dbRouter.doRouter(userId);
            Integer success = transactionTemplate.execute(status -> {
                try {
                    int count;

                    /* ===================
                    // 1. 更新账户总余额
                    ====================*/
                    ActivityAccount activityAccount = new ActivityAccount();
                    activityAccount.setUserId(activityAccountEntity.getUserId());
                    activityAccount.setActivityId(activityAccountEntity.getActivityId());
                    count = activityAccountDao.subtractActivityAccount(activityAccount);
                    if (count == 0) {
                        status.setRollbackOnly();
                        throw new AppException("账户不存在");
                    }

                    /* ===================
                    // 2. 保存/更新账户月余额
                    ====================*/
                    // 1. 先尝试更新
                    ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
                    activityAccountMonth.setUserId(userId);
                    activityAccountMonth.setActivityId(activityAccountMonthEntity.getActivityId());
                    activityAccountMonth.setMonth(activityAccountMonthEntity.getMonth());
                    count = activityAccountMonthDao.subtractActivityAccountMonth(activityAccountMonth);
                    if (count == 0) {
                        // 2. 失败则保存
                        activityAccountMonth.setMonthAllocate(activityAccountMonthEntity.getMonthAllocate());
                        activityAccountMonth.setMonthSurplus(activityAccountMonthEntity.getMonthSurplus() - 1);
                        activityAccountMonthDao.createActivityAccountMonth(activityAccountMonth);

                        // 3. 保持与总余额同步
                        activityAccount.setMonthSurplus(activityAccountMonthEntity.getMonthSurplus() - 1);
                        activityAccountDao.updateActivityAccountMonthSurplus(activityAccount);
                    }

                    /* ===================
                    // 3. 保存/更新账户日余额
                    ====================*/
                    ActivityAccountDay activityAccountDay = new ActivityAccountDay();
                    activityAccountDay.setUserId(userId);
                    activityAccountDay.setActivityId(activityAccountDayEntity.getActivityId());
                    activityAccountDay.setDay(activityAccountDayEntity.getDay());
                    count = activityAccountDayDao.subtractActivityAccountDay(activityAccountDay);
                    if (count == 0) {
                        // 2. 失败则保存
                        activityAccountDay.setDayAllocate(activityAccountDayEntity.getDayAllocate());
                        activityAccountDay.setDaySurplus(activityAccountDayEntity.getDaySurplus() - 1);
                        activityAccountDayDao.createActivityAccountDay(activityAccountDay);

                        // 3. 保持与总余额同步
                        activityAccount.setDaySurplus(activityAccountDayEntity.getDaySurplus() - 1);
                        activityAccountDao.updateActivityAccountDaySurplus(activityAccount);
                    }

                    /* ===================
                    // 4. 保存抽奖订单
                    ====================*/
                    RaffleOrder raffleOrder = new RaffleOrder();
                    raffleOrder.setOrderId(raffleOrderEntity.getOrderId());
                    raffleOrder.setUserId(raffleOrderEntity.getUserId());
                    raffleOrder.setActivityId(raffleOrderEntity.getActivityId());
                    raffleOrder.setStrategyId(raffleOrderEntity.getStrategyId());
                    raffleOrder.setRaffleState(raffleOrderEntity.getRaffleState());
                    raffleOrder.setRaffleTime(raffleOrderEntity.getRaffleTime());
                    raffleOrderDao.saveRaffleOrder(raffleOrder);

                    log.info("【抽奖】账户余额变动：userId={}, activityId={}, total-=1, month-=1, day-=1", userId, activityId);
                    log.info("【抽奖】保存抽奖订单：userId={}, activityId={}, orderId={}", userId, activityId, raffleOrder.getOrderId());
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("【抽奖】保存抽奖订单失败（唯一约束冲突）：orderId={}, error={}", raffleOrderEntity.getOrderId(), e.getMessage());
                    return 0;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【抽奖】保存抽奖订单失败（未知异常）：orderId={}, error={}", raffleOrderEntity.getOrderId(), e.getMessage());
                    return 0;
                }
            });

            if (success != null && success.equals(0)) {
                throw new AppException("保存充值订单失败，orderId=" + raffleOrderEntity.getOrderId());
            }

        } finally {
            dbRouter.clear();
        }
    }

}

package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.event.RechargeSkuStockEmptyEvent;
import com.dasi.domain.activity.model.aggregate.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.model.queue.RechargeSkuStock;
import com.dasi.domain.activity.model.type.ActivityState;
import com.dasi.domain.activity.model.type.RaffleState;
import com.dasi.domain.activity.model.type.RechargeState;
import com.dasi.domain.activity.model.vo.AccountSurplusSnapshot;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.*;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.util.TimeUtil;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
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
        if (rechargeSku == null) throw new AppException("（查询）RechargeSku 不存在：skuId=" + skuId);
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
        List<RechargeSkuEntity> rechargeSkuEntityList = redisService.getValue(cacheKey);
        if (rechargeSkuEntityList != null && !rechargeSkuEntityList.isEmpty()) {
            return rechargeSkuEntityList;
        }

        // 再查数据库
        List<RechargeSku> rechargeSkuList = rechargeSkuDao.queryRechargeSkuByActivityId(activityId);
        if (rechargeSkuList == null || rechargeSkuList.isEmpty()) throw new AppException("（查询）RechargeSkuList 不存在：activityId=" + activityId);
        rechargeSkuEntityList = rechargeSkuList.stream()
                .map(rechargeSku -> RechargeSkuEntity.builder()
                        .skuId(rechargeSku.getSkuId())
                        .activityId(rechargeSku.getActivityId())
                        .quotaId(rechargeSku.getQuotaId())
                        .stockAllocate(rechargeSku.getStockAllocate())
                        .stockSurplus(rechargeSku.getStockSurplus())
                        .build())
                .collect(Collectors.toList());

        // 缓存并返回
        redisService.setValue(cacheKey, rechargeSkuEntityList);
        return rechargeSkuEntityList;

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
        if (activity == null) throw new AppException("（查询）Activity 不存在：activity=Id" + activityId);
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
    public RechargeQuotaEntity queryRechargeQuotaByQuotaId(Long quotaId) {
        // 先查缓存
        String cacheKey = RedisKey.RECHARGE_QUOTA_KEY + quotaId;
        RechargeQuotaEntity rechargeQuotaEntity = redisService.getValue(cacheKey);
        if (rechargeQuotaEntity != null) {
            return rechargeQuotaEntity;
        }

        // 再查数据库
        RechargeQuota rechargeQuota = rechargeQuotaDao.queryRechargeQuotaByQuotaId(quotaId);
        if (rechargeQuota == null) throw new AppException("（查询）RechargeQuota 不存在：quotaId=" + quotaId);
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
        if (activityAccount == null) throw new AppException("（查询）ActivityAccount 不存在：userId=" + userId + ", activityId=" + activityId);
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
        if (activityAccountMonth == null) return null; // 返回 null 去创建
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
        if (activityAccountDay == null) return null; // 返回 null 去创建
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
        return RaffleOrderEntity.builder()
                .orderId(raffleOrder.getOrderId())
                .userId(raffleOrder.getUserId())
                .activityId(raffleOrder.getActivityId())
                .strategyId(raffleOrder.getStrategyId())
                .raffleState(RaffleState.valueOf(raffleOrder.getRaffleState()))
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
    public void sendRechargeSkuStockConsumeToMQ(RechargeSkuStock rechargeSkuStock) {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<RechargeSkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<RechargeSkuStock> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(rechargeSkuStock, 3, TimeUnit.SECONDS);
    }

    @Override
    public RechargeSkuStock getQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<RechargeSkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = RedisKey.RECHARGE_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<RechargeSkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
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
        rechargeOrder.setRechargeState(rechargeOrderEntity.getRechargeState().name());
        rechargeOrder.setRechargeTime(rechargeOrderEntity.getRechargeTime());

        // 账户对象 - 总
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(rechargeOrderEntity.getUserId());
        activityAccount.setActivityId(rechargeOrderEntity.getActivityId());
        activityAccount.setTotalAllocate(rechargeOrderEntity.getTotalCount());
        activityAccount.setTotalSurplus(rechargeOrderEntity.getTotalCount());
        activityAccount.setDayAllocate(rechargeOrderEntity.getDayCount());
        activityAccount.setDaySurplus(rechargeOrderEntity.getDayCount());
        activityAccount.setMonthAllocate(rechargeOrderEntity.getMonthCount());
        activityAccount.setMonthSurplus(rechargeOrderEntity.getMonthCount());

        // 账户对象 - 月
        ActivityAccountMonth activityAccountMonth = new ActivityAccountMonth();
        activityAccountMonth.setActivityId(rechargeOrderEntity.getActivityId());
        activityAccountMonth.setUserId(rechargeOrderEntity.getUserId());
        activityAccountMonth.setMonth(TimeUtil.thisMonth(true));
        activityAccountMonth.setMonthAllocate(rechargeOrderEntity.getMonthCount());
        activityAccountMonth.setMonthSurplus(rechargeOrderEntity.getMonthCount());

        // 账户对象 - 日
        ActivityAccountDay activityAccountDay = new ActivityAccountDay();
        activityAccountDay.setActivityId(rechargeOrderEntity.getActivityId());
        activityAccountDay.setUserId(rechargeOrderEntity.getUserId());
        activityAccountDay.setDay(TimeUtil.thisDay(true));
        activityAccountDay.setDayAllocate(rechargeOrderEntity.getDayCount());
        activityAccountDay.setDaySurplus(rechargeOrderEntity.getDayCount());

        String orderId = rechargeOrderEntity.getOrderId();
        String userId = rechargeOrderEntity.getUserId();
        Long activityId = rechargeOrderEntity.getActivityId();

        try {
            AccountSurplusSnapshot before = getAccountSurplusSnapshot(userId, activityId);

            dbRouter.doRouter(userId);
            Boolean success = transactionTemplate.execute(status -> {
                try {
                    int count;

                    // 1. 保存/更新总账户
                    count = activityAccountDao.rechargeActivityAccount(activityAccount);
                    if (count == 0) {
                        activityAccountDao.createActivityAccount(activityAccount);
                    }

                    // 2. 保存/更新月账户
                    count = activityAccountMonthDao.rechargeActivityAccountMonth(activityAccountMonth);
                    if (count == 0) {
                        activityAccountMonthDao.createActivityAccountMonth(activityAccountMonth);
                    }

                    // 3. 保存/更新日账户
                    count = activityAccountDayDao.rechargeActivityAccountDay(activityAccountDay);
                    if (count == 0) {
                        activityAccountDayDao.createActivityAccountDay(activityAccountDay);
                    }

                    // 4. 写入订单
                    rechargeOrderDao.saveRechargeOrder(rechargeOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【充值】保存充值订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            AccountSurplusSnapshot after = getAccountSurplusSnapshot(userId, activityId);

            if (Boolean.TRUE.equals(success)) {

                rechargeOrder.setRechargeState(RechargeState.USED.name());
                rechargeOrderDao.updateRechargeState(rechargeOrder);

                log.info("【充值】userId={}, activityId={}, total:{}->{}, month:{}->{}, day:{}->{}",
                        userId, activityId,
                        before.getTotalGeneralSurplus(),  after.getTotalGeneralSurplus(),
                        before.getMonthGeneralSurplus(),  after.getMonthGeneralSurplus(),
                        before.getDayGeneralSurplus(),    after.getDayGeneralSurplus()
                );

                log.info("【充值】userId={}, activityId={}, month={}, surplus:{}->{}",
                        userId, activityId,
                        activityAccountMonth.getMonth(),
                        before.getMonthSurplus(), after.getMonthSurplus()
                );

                log.info("【充值】userId={}, activityId={}, day={}, surplus:{}->{}",
                        userId, activityId,
                        activityAccountDay.getDay(),
                        before.getDaySurplus(), after.getDaySurplus()
                );

                log.info("【充值】保存充值订单成功：orderId={}", orderId);

            } else {
                rechargeOrder.setRechargeState(RechargeState.CANCELLED.name());
                rechargeOrderDao.updateRechargeState(rechargeOrder);
                throw new AppException("（充值）保存充值订单失败：orderId=" + orderId);
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

            AccountSurplusSnapshot before = getAccountSurplusSnapshot(userId, activityId);

            dbRouter.doRouter(userId);
            Boolean success = transactionTemplate.execute(status -> {
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
                        log.info("【抽奖】账户不存在：userId={}, activityId={}", userId, activityId);
                        return false;
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
                    raffleOrder.setRaffleState(raffleOrderEntity.getRaffleState().name());
                    raffleOrder.setRaffleTime(raffleOrderEntity.getRaffleTime());
                    raffleOrderDao.saveRaffleOrder(raffleOrder);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("【抽奖】保存抽奖订单时发生错误：error={}", e.getMessage());
                    return false;
                }
            });

            AccountSurplusSnapshot after = getAccountSurplusSnapshot(userId, activityId);

            if (Boolean.TRUE.equals(success)) {

                log.info("【抽奖】userId={}, activityId={}, total:{}->{}, month:{}->{}, day:{}->{}",
                        userId, activityId,
                        before.getTotalGeneralSurplus(),  after.getTotalGeneralSurplus(),
                        before.getMonthGeneralSurplus(),  after.getMonthGeneralSurplus(),
                        before.getDayGeneralSurplus(),    after.getDayGeneralSurplus()
                );

                log.info("【抽奖】userId={}, activityId={}, month={}, surplus:{}->{}",
                        userId, activityId,
                        activityAccountMonthEntity.getMonth(),
                        before.getMonthSurplus(), after.getMonthSurplus()
                );

                log.info("【抽奖】userId={}, activityId={}, day={}, surplus:{}->{}",
                        userId, activityId,
                        activityAccountDayEntity.getDay(),
                        before.getDaySurplus(), after.getDaySurplus()
                );

                log.info("【抽奖】保存抽奖订单成功：orderId={}", raffleOrderEntity.getOrderId());

            } else {
                throw new AppException("（抽奖）保存抽奖订单失败：orderId=" + raffleOrderEntity.getOrderId());
            }

        } finally {
            dbRouter.clear();
        }
    }

    private AccountSurplusSnapshot getAccountSurplusSnapshot(String userId, Long activityId) {

        ActivityAccount accountReq = new ActivityAccount();
        accountReq.setUserId(userId);
        accountReq.setActivityId(activityId);

        ActivityAccountMonth monthReq = new ActivityAccountMonth();
        monthReq.setUserId(userId);
        monthReq.setActivityId(activityId);
        monthReq.setMonth(TimeUtil.thisMonth(true));

        ActivityAccountDay dayReq = new ActivityAccountDay();
        dayReq.setUserId(userId);
        dayReq.setActivityId(activityId);
        dayReq.setDay(TimeUtil.thisDay(true));

        ActivityAccount account = activityAccountDao.queryActivityAccount(accountReq);
        ActivityAccountMonth monthAccount = activityAccountMonthDao.queryActivityAccountMonth(monthReq);
        ActivityAccountDay dayAccount = activityAccountDayDao.queryActivityAccountDay(dayReq);

        return AccountSurplusSnapshot.builder()
                .totalGeneralSurplus(account == null ? 0 : account.getTotalSurplus())
                .monthGeneralSurplus(account == null ? 0 : account.getMonthSurplus())
                .dayGeneralSurplus(account == null ? 0 : account.getDaySurplus())
                .monthSurplus(monthAccount == null ? 0 : monthAccount.getMonthSurplus())
                .daySurplus(dayAccount == null ? 0 : dayAccount.getDaySurplus())
                .build();
    }

}

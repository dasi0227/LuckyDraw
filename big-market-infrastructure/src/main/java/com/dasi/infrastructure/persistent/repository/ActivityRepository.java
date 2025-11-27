package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.event.ActivitySkuStockEmptyEvent;
import com.dasi.domain.activity.model.dto.ActivitySkuStock;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import com.dasi.domain.activity.model.entity.ActivityQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
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
    private IActivitySkuDao activitySkuDao;

    @Resource
    private IActivityQuotaDao activityCountDao;

    @Resource
    private IActivityOrderDao activityOrderDao;

    @Resource
    private IActivityAccountDao activityAccountDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IDBRouterStrategy dbRouter;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private ActivitySkuStockEmptyEvent activitySkuStockEmptyEvent;

    @Override
    public ActivitySkuEntity queryActivitySkuBySkuId(Long skuId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_SKU_KEY + skuId;
        ActivitySkuEntity activitySkuEntity = redisService.getValue(cacheKey);
        if (activitySkuEntity != null) {
            return activitySkuEntity;
        }

        // 再查数据库
        ActivitySku activitySku = activitySkuDao.queryActivitySkuBySku(skuId);
        if (activitySku == null) throw new AppException("ActivitySku 不存在，skuId = " + skuId);
        activitySkuEntity = ActivitySkuEntity.builder()
                .skuId(activitySku.getSkuId())
                .activityId(activitySku.getActivityId())
                .activityQuotaId(activitySku.getActivityQuotaId())
                .stockAllocate(activitySku.getStockAllocate())
                .stockSurplus(activitySku.getStockSurplus())
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, activitySkuEntity);
        return activitySkuEntity;
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
    public ActivityQuotaEntity queryActivityQuotaByActivityQuotaId(Long activityQuotaId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_COUNT_KEY + activityQuotaId;
        ActivityQuotaEntity activityQuotaEntity = redisService.getValue(cacheKey);
        if (activityQuotaEntity != null) {
            return activityQuotaEntity;
        }

        // 再查数据库
        ActivityQuota activityQuota = activityCountDao.queryActivityQuotaByActivityQuotaId(activityQuotaId);
        if (activityQuota == null) throw new AppException("ActivityQuota 不存在，请检查 " + activityQuotaId);
        activityQuotaEntity = ActivityQuotaEntity.builder()
                .activityQuotaId(activityQuota.getActivityQuotaId())
                .totalCount(activityQuota.getTotalCount())
                .dayCount(activityQuota.getDayCount())
                .monthCount(activityQuota.getMonthCount())
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, activityQuotaEntity);
        return activityQuotaEntity;
    }

    @Override
    public void saveActivitySkuOrder(ActivityOrderEntity activityOrderEntity) {
        // 订单对象
        ActivityOrder activityOrder = new ActivityOrder();
        activityOrder.setOrderId(activityOrderEntity.getOrderId());
        activityOrder.setBizId(activityOrderEntity.getBizId());
        activityOrder.setUserId(activityOrderEntity.getUserId());
        activityOrder.setSkuId(activityOrderEntity.getSkuId());
        activityOrder.setStrategyId(activityOrderEntity.getStrategyId());
        activityOrder.setActivityId(activityOrderEntity.getActivityId());
        activityOrder.setActivityQuotaId(activityOrderEntity.getActivityQuotaId());
        activityOrder.setTotalCount(activityOrderEntity.getTotalCount());
        activityOrder.setMonthCount(activityOrderEntity.getMonthCount());
        activityOrder.setDayCount(activityOrderEntity.getDayCount());
        activityOrder.setActivityOrderState(activityOrderEntity.getActivityOrderState());
        activityOrder.setOrderTime(activityOrderEntity.getOrderTime());

        // 账户对象
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(activityOrderEntity.getUserId());
        activityAccount.setActivityId(activityOrderEntity.getActivityId());
        activityAccount.setTotalAllocate(activityOrderEntity.getTotalCount());
        activityAccount.setTotalSurplus(activityOrderEntity.getTotalCount());
        activityAccount.setDayAllocate(activityOrderEntity.getDayCount());
        activityAccount.setDaySurplus(activityOrderEntity.getDayCount());
        activityAccount.setMonthAllocate(activityOrderEntity.getMonthCount());
        activityAccount.setMonthSurplus(activityOrderEntity.getMonthCount());

        try {
            dbRouter.doRouter(activityOrderEntity.getUserId());
            transactionTemplate.execute(status -> {
                try {
                    // 1. 写入订单
                    activityOrderDao.insert(activityOrder);
                    // 2. 创建/更新账户
                    int count = activityAccountDao.updateAccountQuota(activityAccount);
                    if (count == 0) {
                        activityAccountDao.insertActivityAccount(activityAccount);
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.warn("【订单创建失败】唯一约束冲突：orderId={}, bizId={}, error={}", activityOrderEntity.getOrderId(), activityOrderEntity.getBizId(), e.getMessage());
                    throw new AppException("唯一索引冲突：" + activityOrderEntity.getOrderId());
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.warn("【订单创建失败】未知异常：error={}", e.getMessage());
                    throw new AppException("订单创建失败，orderId=" + activityOrderEntity.getOrderId());
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public void cacheActivitySkuStockSurplus(Long skuId, Integer stockSurplus) {
        String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_SURPLUS_KEY + skuId;
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, Long.valueOf(stockSurplus));
    }

    // -1 表示为空，-2 表示错误
    @Override
    public Long subtractActivitySkuStockSurplus(Long skuId, LocalDateTime endTime) {
        String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_SURPLUS_KEY + skuId;
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
            eventPublisher.publish(activitySkuStockEmptyEvent.topic(), activitySkuStockEmptyEvent.buildEventMessage(skuId));
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
    public void sendActivitySkuStockConsumeToMQ(ActivitySkuStock activitySkuStock) {
        String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<ActivitySkuStock> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(activitySkuStock, 3, TimeUnit.SECONDS);
    }

    @Override
    public ActivitySkuStock getQueueValue() {
        String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void clearQueueValue() {
        String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_QUEUE_KEY;
        RBlockingQueue<ActivitySkuStock> blockingQueue = redisService.getBlockingQueue(cacheKey);
        blockingQueue.clear();
    }

    @Override
    public void updateActivitySkuStock(Long skuId) {
        activitySkuDao.updateActivitySkuStock(skuId);
    }

    @Override
    public void clearActivitySkuStock(Long skuId) {
        activitySkuDao.clearActivitySkuStock(skuId);
    }
}

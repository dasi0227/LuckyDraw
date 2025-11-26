package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.activity.event.ActivitySkuStockEmptyEvent;
import com.dasi.domain.activity.model.dto.ActivitySkuStock;
import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.dto.SkuOrder;
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
    private IActivityCountDao activityCountDao;

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
    public ActivitySkuEntity queryActivitySkuBySku(Long sku) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_SKU_KEY + sku;
        ActivitySkuEntity activitySkuEntity = redisService.getValue(cacheKey);
        if (activitySkuEntity != null) {
            return activitySkuEntity;
        }

        // 再查数据库
        ActivitySku activitySku = activitySkuDao.queryActivitySkuBySku(sku);
        if (activitySku == null) throw new AppException("ActivitySku 不存在，请检查 " + sku);
        activitySkuEntity = ActivitySkuEntity.builder()
                .sku(activitySku.getSku())
                .activityId(activitySku.getActivityId())
                .activityCountId(activitySku.getActivityCountId())
                .stockAmount(activitySku.getStockAmount())
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
                .beginTime(activity.getBeginTime())
                .endTime(activity.getEndTime())
                .activityCountId(activity.getActivityCountId())
                .strategyId(activity.getStrategyId())
                .state(activity.getState())
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityCountEntity queryActivityCountByActivityCountId(Long activityCountId) {
        // 先查缓存
        String cacheKey = RedisKey.ACTIVITY_COUNT_KEY + activityCountId;
        ActivityCountEntity activityCountEntity = redisService.getValue(cacheKey);
        if (activityCountEntity != null) {
            return activityCountEntity;
        }

        // 再查数据库
        ActivityCount activityCount = activityCountDao.queryActivityCountByActivityCountId(activityCountId);
        if (activityCount == null) throw new AppException("ActivityCount 不存在，请检查 " + activityCountId);
        activityCountEntity = ActivityCountEntity.builder()
                .activityCountId(activityCount.getActivityCountId())
                .totalCount(activityCount.getTotalCount())
                .dayCount(activityCount.getDayCount())
                .monthCount(activityCount.getMonthCount())
                .build();

        // 缓存并返回
        redisService.setValue(cacheKey, activityCountEntity);
        return activityCountEntity;
    }

    @Override
    public void saveOrder(SkuOrder skuOrder) {
        // 订单对象
        ActivityOrderEntity activityOrderEntity = skuOrder.getActivityOrderEntity();
        ActivityOrder activityOrder = new ActivityOrder();
        activityOrder.setOrderId(activityOrderEntity.getOrderId());
        activityOrder.setBizId(activityOrderEntity.getBizId());
        activityOrder.setUserId(activityOrderEntity.getUserId());
        activityOrder.setSku(activityOrderEntity.getSku());
        activityOrder.setStrategyId(activityOrderEntity.getStrategyId());
        activityOrder.setActivityId(activityOrderEntity.getActivityId());
        activityOrder.setActivityName(activityOrderEntity.getActivityName());
        activityOrder.setTotalCount(activityOrderEntity.getTotalCount());
        activityOrder.setMonthCount(activityOrderEntity.getMonthCount());
        activityOrder.setDayCount(activityOrderEntity.getDayCount());
        activityOrder.setState(activityOrderEntity.getState());
        activityOrder.setOrderTime(activityOrderEntity.getOrderTime());

        // 账户对象
        ActivityAccount activityAccount = new ActivityAccount();
        activityAccount.setUserId(skuOrder.getUserId());
        activityAccount.setActivityId(skuOrder.getActivityId());
        activityAccount.setTotalAmount(skuOrder.getTotalCount());
        activityAccount.setTotalSurplus(skuOrder.getTotalCount());
        activityAccount.setDayAmount(skuOrder.getDayCount());
        activityAccount.setDaySurplus(skuOrder.getDayCount());
        activityAccount.setMonthAmount(skuOrder.getMonthCount());
        activityAccount.setMonthSurplus(skuOrder.getMonthCount());

        try {
            dbRouter.doRouter(skuOrder.getUserId());
            transactionTemplate.execute(status -> {
                try {
                    // 1. 写入订单
                    activityOrderDao.insert(activityOrder);
                    // 2. 更新账户
                    int count = activityAccountDao.updateAccountQuota(activityAccount);
                    // 3. 创建账户
                    if (count == 0) {
                        activityAccountDao.insert(activityAccount);
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("唯一索引冲突；{}", skuOrder);
                    throw new AppException("唯一索引冲突：" + skuOrder);
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public void cacheActivitySkuStockSurplus(Long sku, Integer stockSurplus) {
        String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_SURPLUS_KEY + sku;
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, stockSurplus);
    }

    @Override
    public Long subtractActivitySkuStock(Long sku, LocalDateTime endDatetime) {
        String cacheKey = RedisKey.ACTIVITY_SKU_STOCK_SURPLUS_KEY + sku;
        if (!redisService.isExists(cacheKey)) return -1L;
        long surplus = redisService.decr(cacheKey);
        if (surplus == 0) {
            eventPublisher.publish(activitySkuStockEmptyEvent.topic(), activitySkuStockEmptyEvent.buildEventMessage(sku));
            return surplus;
        } else if (surplus < 0) {
            redisService.setAtomicLong(cacheKey, 0);
            return -1L;
        }

        String lockKey = cacheKey + Delimiter.UNDERSCORE + surplus;
        Duration expire = Duration.ofMillis(
                endDatetime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                - System.currentTimeMillis()
                + TimeUnit.DAYS.toMillis(1));
        return redisService.setNx(lockKey, expire) ? surplus : -1L;
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
    public void updateActivitySkuStock(Long sku) {
        activitySkuDao.updateActivitySkuStock(sku);
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activitySkuDao.clearActivitySkuStock(sku);
    }
}

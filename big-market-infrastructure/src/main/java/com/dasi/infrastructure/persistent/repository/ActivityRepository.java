package com.dasi.infrastructure.persistent.repository;

import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.infrastructure.persistent.dao.*;
import com.dasi.infrastructure.persistent.po.Activity;
import com.dasi.infrastructure.persistent.po.ActivityCount;
import com.dasi.infrastructure.persistent.po.ActivitySku;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

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
}

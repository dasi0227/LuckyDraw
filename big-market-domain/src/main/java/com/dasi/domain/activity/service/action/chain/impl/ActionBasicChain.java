package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.model.entity.ActivityQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.type.ActivityState;
import com.dasi.domain.activity.service.action.chain.AbstractActionChain;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component(ActionModel.ACTION_BASIC)
public class ActionBasicChain extends AbstractActionChain {

    @Override
    public void action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityQuotaEntity activityQuotaEntity) {

        String activityState = activityEntity.getActivityState();
        if (ActivityState.CREATED.getCode().equals(activityState)) {
            log.info("【活动责任链 - action_basic】活动未开启：activityId = {}, activityState = {}", activityEntity.getActivityId(), activityState);
            throw new AppException("活动还未开始");
        }
        if (ActivityState.OVER.getCode().equals(activityState)) {
            log.info("【活动责任链 - action_basic】活动关闭中：activityId = {}, activityState = {}", activityEntity.getActivityId(), activityState);
            throw new AppException("活动关闭中");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activityEntity.getActivityBeginTime())) {
            log.info("【活动责任链 - action_basic】还未到开始时间：activityId = {}, beginTime = {}", activityEntity.getActivityId(), activityEntity.getActivityBeginTime());
            throw new AppException("活动还未到开始时间");
        }
        if (now.isAfter(activityEntity.getActivityEndTime())) {
            log.info("【活动责任链 - action_basic】超过了截止时间：activityId = {}, endTime = {}", activityEntity.getActivityId(), activityEntity.getActivityEndTime());
            throw new AppException("超过了截止时间");
        }

        Integer surplus = activitySkuEntity.getStockSurplus();
        if (surplus <= 0) {
            log.info("【活动责任链 - action_basic】活动被抢光了：activityId = {}, surplus = {}", activityEntity.getActivityId(), surplus);
            throw new AppException("活动被抢光了");
        }

        log.info("【活动责任链 - action_basic】活动基础信息无误：activityId = {}, activityName = {}", activityEntity.getActivityId(), activityEntity.getActivityName());
        next().action(activitySkuEntity, activityEntity, activityQuotaEntity);
    }

}

package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.model.type.ActivityState;
import com.dasi.domain.activity.service.action.chain.AbstractActionChain;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component(ActionModel.ACTION_BASIC_INFO)
public class ActionBasicInfoChain extends AbstractActionChain {

    @Override
    public void action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {

        String state = activityEntity.getState();
        if (ActivityState.CREATED.getCode().equals(state)) {
            log.info("【活动责任链 - action_basic_info】活动未开启：activityId = {}, activityState = {}", activityEntity.getActivityId(), state);
            throw new AppException("活动还未开始");
        }
        if (ActivityState.CLOSE.getCode().equals(state)) {
            log.info("【活动责任链 - action_basic_info】活动关闭中：activityId = {}, activityState = {}", activityEntity.getActivityId(), state);
            throw new AppException("活动关闭中");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activityEntity.getBeginTime())) {
            log.info("【活动责任链 - action_basic_info】还未到开始时间：activityId = {}, beginTime = {}", activityEntity.getActivityId(), activityEntity.getBeginTime());
            throw new AppException("活动还未到开始时间");
        }
        if (now.isAfter(activityEntity.getEndTime())) {
            log.info("【活动责任链 - action_basic_info】超过了截止时间：activityId = {}, endTime = {}", activityEntity.getActivityId(), activityEntity.getEndTime());
            throw new AppException("超过了截止时间");
        }

        Integer surplus = activitySkuEntity.getStockSurplus();
        if (surplus <= 0) {
            log.info("【活动责任链 - action_basic_info】活动被抢光了：activityId = {}, surplus = {}", activityEntity.getActivityId(), surplus);
            throw new AppException("活动被抢光了");
        }

        next().action(activitySkuEntity, activityEntity, activityCountEntity);
    }

}

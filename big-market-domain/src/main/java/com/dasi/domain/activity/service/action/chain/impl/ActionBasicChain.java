package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.model.entity.RechargeQuotaEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.RechargeSkuEntity;
import com.dasi.domain.activity.model.type.ActivityState;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component(ActionModel.ACTION_BASIC)
public class ActionBasicChain extends AbstractActionChain {

    @Override
    public Boolean action(RechargeSkuEntity rechargeSkuEntity, ActivityEntity activityEntity, RechargeQuotaEntity rechargeQuotaEntity) {

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

        log.info("【活动责任链 - action_basic】活动基础信息无误：activityId = {}, activityName = {}", activityEntity.getActivityId(), activityEntity.getActivityName());
        return next().action(rechargeSkuEntity, activityEntity, rechargeQuotaEntity);
    }

}

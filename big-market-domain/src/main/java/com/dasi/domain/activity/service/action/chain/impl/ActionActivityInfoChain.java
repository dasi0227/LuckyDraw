package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.model.type.ActivityState;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component(ActionModel.ACTIVITY_INFO)
public class ActionActivityInfoChain extends AbstractActionChain {

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {

        ActivityEntity activityEntity = actionChainCheckAggregate.getActivityEntity();

        String activityState = activityEntity.getActivityState();
        if (ActivityState.CREATED.getCode().equals(activityState)) {
            log.info("【活动责任链】activity_info 接管，活动未开启：activityId={}, activityState={}", activityEntity.getActivityId(), activityState);
            throw new AppException("活动还未开始");
        }
        if (ActivityState.OVER.getCode().equals(activityState)) {
            log.info("【活动责任链】activity_info 接管，活动已结束：activityId={}, activityState={}", activityEntity.getActivityId(), activityState);
            throw new AppException("活动关闭中");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activityEntity.getActivityBeginTime())) {
            log.info("【活动责任链】activity_info 接管，还未到开始时间：activityId={}, beginTime={}", activityEntity.getActivityId(), activityEntity.getActivityBeginTime());
            throw new AppException("活动还未到开始时间");
        }
        if (now.isAfter(activityEntity.getActivityEndTime())) {
            log.info("【活动责任链】activity_info 接管，超过了截止时间：activityId={}, endTime={}", activityEntity.getActivityId(), activityEntity.getActivityEndTime());
            throw new AppException("超过了截止时间");
        }

        if (ActivityState.UNDERWAY.getCode().equals(activityState)) {
            log.info("【活动责任链】activity_info 放行，活动基础信息无误：activityId={}, activityName={}, activityState={}", activityEntity.getActivityId(), activityEntity.getActivityName(), activityEntity.getActivityState());
        }

        return next().action(actionChainCheckAggregate);
    }

}

package com.dasi.domain.activity.service.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.type.ActionModel;
import com.dasi.domain.activity.model.type.ActivityState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component(ActionModel.ACTIVITY_INFO)
public class ActivityInfoChain extends AbstractActivityChain {

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {

        ActivityEntity activityEntity = actionChainCheckAggregate.getActivityEntity();

        ActivityState activityState = activityEntity.getActivityState();
        if (ActivityState.CREATED.equals(activityState)) {
            log.info("【检查】ACTIVITY_INFO 拦截（活动未开启）：activityId={}, activityState={}", activityEntity.getActivityId(), activityState);
            return false;
        }
        if (ActivityState.OVER.equals(activityState)) {
            log.info("【检查】ACTIVITY_INFO 拦截（活动已结束）：activityId={}, activityState={}", activityEntity.getActivityId(), activityState);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activityEntity.getActivityBeginTime())) {
            log.info("【检查】ACTIVITY_INFO 拦截（活动未开启）：activityId={}, beginTime={}", activityEntity.getActivityId(), activityEntity.getActivityBeginTime());
            return false;
        }
        if (now.isAfter(activityEntity.getActivityEndTime())) {
            log.info("【检查】ACTIVITY_INFO 拦截（活动已结束）：activityId={}, endTime={}", activityEntity.getActivityId(), activityEntity.getActivityEndTime());
            return false;
        }

        log.info("【检查】ACTIVITY_INFO 放行：activityId={}, activityName={}, activityState={}", activityEntity.getActivityId(), activityEntity.getActivityName(), activityEntity.getActivityState());
        return next().action(actionChainCheckAggregate);
    }

}

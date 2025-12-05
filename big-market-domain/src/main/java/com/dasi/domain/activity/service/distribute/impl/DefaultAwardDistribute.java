package com.dasi.domain.activity.service.distribute.impl;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.activity.event.DistributeActivityAwardEvent;
import com.dasi.domain.activity.event.DistributeActivityAwardEvent.DistributeActivityAwardMessage;
import com.dasi.domain.activity.model.entity.ActivityAwardEntity;
import com.dasi.domain.activity.model.entity.TaskEntity;
import com.dasi.domain.activity.model.io.DistributeContext;
import com.dasi.domain.activity.model.io.DistributeResult;
import com.dasi.domain.activity.model.type.AwardState;
import com.dasi.domain.activity.model.type.TaskState;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.distribute.IAwardDistribute;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DefaultAwardDistribute implements IAwardDistribute {

    @Resource
    private IActivityRepository activityRepository;

    @Resource
    private DistributeActivityAwardEvent distributeActivityAwardEvent;

    @Override
    public DistributeResult doAwardDistribute(DistributeContext distributeContext) {

        // 1. 参数校验
        String userId = distributeContext.getUserId();
        Long activityId = distributeContext.getActivityId();
        String orderId = distributeContext.getOrderId();
        Long awardId = distributeContext.getAwardId();
        if (StringUtils.isBlank(userId)) throw new AppException("（抽奖）缺少参数 userId");
        if (activityId == null) throw new AppException("（抽奖）缺少参数 activityId");
        if (StringUtils.isBlank(orderId)) throw new AppException("（抽奖）缺少参数 orderId");
        if (awardId == null) throw new AppException("（抽奖）缺少参数 awardId");

        // 2. 构建消息对象
        DistributeActivityAwardMessage distributeActivityAwardMessage = DistributeActivityAwardMessage.builder()
                .userId(distributeContext.getUserId())
                .awardId(distributeContext.getAwardId())
                .orderId(distributeContext.getOrderId())
                .build();
        BaseEvent.EventMessage<DistributeActivityAwardMessage> eventMessage = distributeActivityAwardEvent.buildEventMessage(distributeActivityAwardMessage);

        // 3. 构建任务对象
        TaskEntity taskEntity = TaskEntity.builder()
                .userId(distributeContext.getUserId())
                .messageId(eventMessage.getMessageId())
                .topic(distributeActivityAwardEvent.getTopic())
                .message(JSON.toJSONString(eventMessage))
                .taskState(TaskState.CREATED)
                .build();

        // 4. 保存
        ActivityAwardEntity activityAwardEntity = ActivityAwardEntity.builder()
                .userId(distributeContext.getUserId())
                .activityId(distributeContext.getActivityId())
                .orderId(distributeContext.getOrderId())
                .awardId(distributeContext.getAwardId())
                .awardName(distributeContext.getAwardName())
                .awardTime(LocalDateTime.now())
                .awardState(AwardState.CREATED)
                .build();
        activityRepository.saveActivityAward(activityAwardEntity, taskEntity);

        return DistributeResult.builder().awardId(activityAwardEntity.getAwardId()).awardName(activityAwardEntity.getAwardName()).messageId(taskEntity.getMessageId()).build();
    }

    @Override
    public void updateActivityAwardState(ActivityAwardEntity activityAwardEntity) {
        activityRepository.updateActivityAwardState(activityAwardEntity);
    }

}

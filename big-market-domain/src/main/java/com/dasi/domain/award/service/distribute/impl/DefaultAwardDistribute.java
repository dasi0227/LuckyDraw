package com.dasi.domain.award.service.distribute.impl;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.award.event.DispatchActivityAwardEvent;
import com.dasi.domain.award.event.DispatchActivityAwardEvent.DispatchActivityAwardMessage;
import com.dasi.domain.award.model.entity.ActivityAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.io.DistributeContext;
import com.dasi.domain.award.model.io.DistributeResult;
import com.dasi.domain.award.model.type.AwardState;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.domain.award.model.entity.AwardEntity;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;


@Service
public class DefaultAwardDistribute implements IAwardDistribute {

    @Resource
    private IAwardRepository awardRepository;

    @Resource
    private DispatchActivityAwardEvent dispatchActivityAwardEvent;

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
        DispatchActivityAwardMessage dispatchActivityAwardMessage = DispatchActivityAwardMessage.builder()
                .userId(distributeContext.getUserId())
                .awardId(distributeContext.getAwardId())
                .orderId(distributeContext.getOrderId())
                .build();
        BaseEvent.EventMessage<DispatchActivityAwardMessage> eventMessage = dispatchActivityAwardEvent.buildEventMessage(dispatchActivityAwardMessage);

        // 3. 构建任务对象
        TaskEntity taskEntity = TaskEntity.builder()
                .userId(distributeContext.getUserId())
                .messageId(eventMessage.getMessageId())
                .topic(dispatchActivityAwardEvent.getTopic())
                .message(JSON.toJSONString(eventMessage))
                .taskState(TaskState.CREATED)
                .build();

        // 4. 查询奖品信息
        AwardEntity awardEntity = awardRepository.queryAwardByAwardId(awardId);

        // 5. 保存中奖记录
        ActivityAwardEntity activityAwardEntity = ActivityAwardEntity.builder()
                .userId(distributeContext.getUserId())
                .activityId(distributeContext.getActivityId())
                .orderId(distributeContext.getOrderId())
                .awardId(distributeContext.getAwardId())
                .awardName(awardEntity.getAwardName())
                .awardTime(LocalDateTime.now())
                .awardState(AwardState.CREATED)
                .build();
        awardRepository.saveActivityAward(activityAwardEntity, taskEntity);

        return DistributeResult.builder()
                .messageId(taskEntity.getMessageId())
                .awardId(activityAwardEntity.getAwardId())
                .awardName(awardEntity.getAwardName())
                .awardType(awardEntity.getAwardType().name())
                .build();
    }

}
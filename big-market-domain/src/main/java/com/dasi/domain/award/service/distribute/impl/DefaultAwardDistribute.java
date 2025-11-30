package com.dasi.domain.award.service.distribute.impl;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.award.event.DistributeRaffleAwardEvent;
import com.dasi.domain.award.event.DistributeRaffleAwardMessage;
import com.dasi.domain.award.model.dto.DistributeContext;
import com.dasi.domain.award.model.dto.DistributeResult;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.AwardState;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.types.event.BaseEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class DefaultAwardDistribute implements IAwardDistribute {

    @Resource
    private IAwardRepository awardRepository;

    @Resource
    private DistributeRaffleAwardEvent distributeRaffleAwardEvent;

    @Override
    public DistributeResult doAwardDistribute(DistributeContext distributeContext) {
        // 1. 构建消息对象
        DistributeRaffleAwardMessage distributeRaffleAwardMessage = DistributeRaffleAwardMessage.builder()
                .userId(distributeContext.getUserId())
                .awardId(distributeContext.getAwardId())
                .build();
        BaseEvent.EventMessage<DistributeRaffleAwardMessage> eventMessage = distributeRaffleAwardEvent.buildEventMessage(distributeRaffleAwardMessage);

        // 2. 构建任务对象
        TaskEntity taskEntity = TaskEntity.builder()
                .userId(distributeContext.getUserId())
                .messageId(eventMessage.getId())
                .topic(distributeRaffleAwardEvent.topic())
                .message(JSON.toJSONString(eventMessage))
                .taskState(TaskState.CREATED.getCode())
                .build();

        // 3. 保存
        RaffleAwardEntity raffleAwardEntity = RaffleAwardEntity.builder()
                .userId(distributeContext.getUserId())
                .activityId(distributeContext.getActivityId())
                .strategyId(distributeContext.getStrategyId())
                .orderId(distributeContext.getOrderId())
                .awardId(distributeContext.getAwardId())
                .awardName(distributeContext.getAwardName())
                .awardTime(LocalDateTime.now())
                .awardState(AwardState.CREATED.getCode())
                .build();
        awardRepository.saveRaffleAward(raffleAwardEntity, taskEntity);

        return DistributeResult.builder().awardId(raffleAwardEntity.getAwardId()).awardName(raffleAwardEntity.getAwardName()).messageId(taskEntity.getMessageId()).build();
    }

}

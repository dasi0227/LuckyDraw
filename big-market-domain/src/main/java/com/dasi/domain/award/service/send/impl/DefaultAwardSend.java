package com.dasi.domain.award.service.send.impl;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.award.event.SendRaffleAwardEvent;
import com.dasi.domain.award.event.SendRaffleAwardMessage;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.send.IAwardSend;
import com.dasi.types.event.BaseEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultAwardSend implements IAwardSend {

    @Resource
    private IAwardRepository awardRepository;

    @Resource
    private SendRaffleAwardEvent sendRaffleAwardEvent;

    @Override
    public void saveRaffleAward(RaffleAwardEntity raffleAwardEntity) {
        // 1. 构建消息对象
        SendRaffleAwardMessage sendRaffleAwardMessage = SendRaffleAwardMessage.builder()
                .userId(raffleAwardEntity.getUserId())
                .awardId(raffleAwardEntity.getAwardId())
                .build();
        BaseEvent.EventMessage<SendRaffleAwardMessage> eventMessage = sendRaffleAwardEvent.buildEventMessage(sendRaffleAwardMessage);

        // 2. 构建任务对象
        TaskEntity taskEntity = TaskEntity.builder()
                .userId(raffleAwardEntity.getUserId())
                .messageId(eventMessage.getId())
                .topic(sendRaffleAwardEvent.topic())
                .message(JSON.toJSONString(eventMessage))
                .taskState(TaskState.CREATED.getCode())
                .build();

        // 3. 保存
        awardRepository.saveRaffleAward(raffleAwardEntity, taskEntity);

    }

}

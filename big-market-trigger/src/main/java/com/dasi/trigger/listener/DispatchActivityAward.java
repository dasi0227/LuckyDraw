package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.award.event.DispatchActivityAwardEvent.DispatchActivityAwardMessage;
import com.dasi.domain.award.model.io.DispatchContext;
import com.dasi.domain.award.model.io.DispatchResult;
import com.dasi.domain.award.service.dispatch.IAwardDispatch;
import com.dasi.types.event.BaseEvent.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchActivityAward {

    @Resource
    private IAwardDispatch awardDispatch;

    @RabbitListener(queuesToDeclare = @Queue(value = "dispatch_raffle_award"))
    public void deliverActivityAward(String message) {
        EventMessage<DispatchActivityAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<DispatchActivityAwardMessage>>() {}.getType());
        DispatchActivityAwardMessage dispatchActivityAwardMessage = eventMessage.getData();

        DispatchContext dispatchContext = DispatchContext.builder()
                .userId(dispatchActivityAwardMessage.getUserId())
                .orderId(dispatchActivityAwardMessage.getOrderId())
                .awardId(dispatchActivityAwardMessage.getAwardId())
                .build();

        DispatchResult dispatchResult = awardDispatch.doAwardDispatch(dispatchContext);
        log.info("【投递】{}", dispatchResult);
    }

}
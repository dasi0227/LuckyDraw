package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.award.event.DistributeActivityAwardEvent.DistributeActivityAwardMessage;
import com.dasi.domain.award.model.io.DeliverContext;
import com.dasi.domain.award.model.io.DeliverResult;
import com.dasi.domain.award.service.deliver.IAwardDeliver;
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
    private IAwardDeliver awardDeliver;

    @RabbitListener(queuesToDeclare = @Queue(value = "distribute_raffle_award"))
    public void dispatchActivityAward(String message) {
        EventMessage<DistributeActivityAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<DistributeActivityAwardMessage>>() {}.getType());
        DistributeActivityAwardMessage distributeActivityAwardMessage = eventMessage.getData();

        DeliverContext deliverContext = DeliverContext.builder()
                .userId(distributeActivityAwardMessage.getUserId())
                .orderId(distributeActivityAwardMessage.getOrderId())
                .awardId(distributeActivityAwardMessage.getAwardId())
                .build();

        DeliverResult deliverResult = awardDeliver.doAwardDeliver(deliverContext);
        log.info("【投递】{}", deliverResult);
    }

}

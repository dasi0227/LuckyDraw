package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.award.event.DistributeRaffleAwardMessage;
import com.dasi.domain.award.model.entity.RaffleAwardEntity;
import com.dasi.domain.award.model.type.AwardState;
import com.dasi.domain.award.service.distribute.IAwardDistribute;
import com.dasi.types.event.BaseEvent.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchRaffleAward {

    @Value("${spring.rabbitmq.topic.distribute_raffle_award}")
    private String topic;

    @Resource
    private IAwardDistribute awardDistribute;

    @RabbitListener(queuesToDeclare = @Queue(value = "distribute_raffle_award"))
    public void dispatchRaffleAward(String message) {
        try {
            EventMessage<DistributeRaffleAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<DistributeRaffleAwardMessage>>() {}.getType());
            DistributeRaffleAwardMessage distributeRaffleAwardMessage = eventMessage.getData();
            RaffleAwardEntity raffleAwardEntity = RaffleAwardEntity.builder()
                    .userId(distributeRaffleAwardMessage.getUserId())
                    .awardId(distributeRaffleAwardMessage.getAwardId())
                    .orderId(distributeRaffleAwardMessage.getOrderId())
                    .awardState(AwardState.COMPLETED.getCode())
                    .build();
            int count = awardDistribute.updateRaffleAwardState(raffleAwardEntity);
            if (count == 1) {
                log.info("【监听消息】发放奖品到用户成功：topic={}, userId={}, orderId={}", topic, distributeRaffleAwardMessage.getUserId(), distributeRaffleAwardMessage.getOrderId());
            } else {
                raffleAwardEntity.setAwardState(AwardState.FAILED.getCode());
                awardDistribute.updateRaffleAwardState(raffleAwardEntity);
                log.info("【监听消息】发放奖品到用户失败：topic={}, userId={}, orderId={}", topic, distributeRaffleAwardMessage.getUserId(), distributeRaffleAwardMessage.getOrderId());
            }
        } catch (Exception e) {
            log.info("【监听消息】发放奖品到用户失败：topic={}, message={}", topic, message);
            throw new RuntimeException(e);
        }
    }

}

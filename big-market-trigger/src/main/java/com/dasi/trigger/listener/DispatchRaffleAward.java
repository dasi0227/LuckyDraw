package com.dasi.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DispatchRaffleAward {

    @Value("${spring.rabbitmq.topic.distribute_raffle_award}")
    private String topic;

    @RabbitListener(queuesToDeclare = @Queue(value = "distribute_raffle_award"))
    public void dispatchRaffleAward(String message) {
        try {
            log.info("【监听消息 - dispatchRaffleAward】发送奖品到用户成功：topic = {}, message = {}", topic, message);
        } catch (Exception e) {
            log.info("【监听消息 - dispatchRaffleAward】发送奖品到用户失败：topic = {}, message = {}", topic, message);
            throw new RuntimeException(e);
        }
    }

}

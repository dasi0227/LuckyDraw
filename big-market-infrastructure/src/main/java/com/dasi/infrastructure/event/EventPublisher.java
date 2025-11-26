package com.dasi.infrastructure.event;

import com.alibaba.fastjson.JSON;
import com.dasi.types.event.BaseEvent.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class EventPublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void publish(String topic, EventMessage<?> eventMessage) {
        try {
            String message = JSON.toJSONString(eventMessage);
            rabbitTemplate.convertAndSend(topic, message);
            log.error("【消息队列 - Publisher】发送消息成功：topic = {}, message = {}", topic, message);
        } catch (Exception e) {
            log.error("【消息队列 - Publisher】发送消息失败：topic = {}, errorMsg = {}", topic, e.getMessage());
            throw e;
        }
    }


}

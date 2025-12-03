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
        String message = JSON.toJSONString(eventMessage);
        publish(topic, message);
    }

    public void publish(String topic, String message) {
        try {
            rabbitTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("【发送】发送消息失败：topic={}, message={}, error={}", topic, message, e.getMessage());
            throw e;
        }
    }


}

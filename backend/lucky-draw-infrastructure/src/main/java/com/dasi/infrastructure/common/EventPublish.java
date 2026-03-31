package com.dasi.infrastructure.common;

import com.dasi.domain.common.IEventPublish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class EventPublish implements IEventPublish {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void publish(String topic, String message) {
        try {
            rabbitTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("【发送】发送消息失败：topic={}, message={}, error={}", topic, message, e.getMessage());
            throw e;
        }
    }

}

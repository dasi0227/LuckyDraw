package com.dasi.domain.award.event;

import com.dasi.types.event.BaseEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SendRaffleAwardEvent extends BaseEvent<SendRaffleAwardMessage> {

    @Value("${spring.rabbitmq.topic.send_raffle_award}")
    private String topic;

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public EventMessage<SendRaffleAwardMessage> buildEventMessage(SendRaffleAwardMessage data) {
        return EventMessage.<SendRaffleAwardMessage>builder()
                .id(RandomStringUtils.randomNumeric(12))
                .time(LocalDateTime.now())
                .data(data)
                .build();
    }
}

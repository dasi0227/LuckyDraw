package com.dasi.domain.award.event;

import com.dasi.types.event.BaseEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DistributeRaffleAwardEvent extends BaseEvent<DistributeRaffleAwardMessage> {

    @Value("${spring.rabbitmq.topic.distribute_raffle_award}")
    private String topic;

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public EventMessage<DistributeRaffleAwardMessage> buildEventMessage(DistributeRaffleAwardMessage data) {
        return EventMessage.<DistributeRaffleAwardMessage>builder()
                .id(RandomStringUtils.randomNumeric(12))
                .time(LocalDateTime.now())
                .data(data)
                .build();
    }
}

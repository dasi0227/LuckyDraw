package com.dasi.domain.award.event;

import com.dasi.types.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DistributeRaffleAwardEvent extends BaseEvent<DistributeRaffleAwardEvent.DistributeRaffleAwardMessage> {

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributeRaffleAwardMessage {

        private String userId;

        private Long awardId;

        private String orderId;

    }

}

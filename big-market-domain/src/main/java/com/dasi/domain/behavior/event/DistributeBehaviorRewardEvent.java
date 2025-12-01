package com.dasi.domain.behavior.event;

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
public class DistributeBehaviorRewardEvent extends BaseEvent<DistributeBehaviorRewardEvent.DistributeBehaviorRewardMessage> {

    @Value("${spring.rabbitmq.topic.distribute_behavior_reward}")
    private String topic;

    @Override
    public String getTopic() {
        return topic;
    }


    @Override
    public EventMessage<DistributeBehaviorRewardMessage> buildEventMessage(DistributeBehaviorRewardMessage data) {
        return EventMessage.<DistributeBehaviorRewardMessage>builder()
                .messageId(RandomStringUtils.randomNumeric(12))
                .time(LocalDateTime.now())
                .data(data)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributeBehaviorRewardMessage {

        private String userId;

        private String bizId;

        private String behavior_reward;

        private String behavior_config;

    }
}

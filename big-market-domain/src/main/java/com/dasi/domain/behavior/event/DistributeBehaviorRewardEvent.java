package com.dasi.domain.behavior.event;

import com.dasi.domain.behavior.model.type.RewardType;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DistributeBehaviorRewardEvent extends BaseEvent<DistributeBehaviorRewardEvent.DistributeBehaviorRewardMessage> {

    @Value("${spring.rabbitmq.topic.distribute_behavior_reward}")
    private String topic;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public EventMessage<DistributeBehaviorRewardMessage> buildEventMessage(DistributeBehaviorRewardMessage data) {
        return EventMessage.<DistributeBehaviorRewardMessage>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisTime(true))
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

        private String orderId;

        private RewardType rewardType;

        private String rewardValue;

    }
}

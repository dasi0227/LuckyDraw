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
public class DispatchBehaviorRewardEvent extends BaseEvent<DispatchBehaviorRewardEvent.DispatchBehaviorRewardMessage> {

    @Value("${spring.rabbitmq.topic.dispatch_behavior_reward}")
    private String topic;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public EventMessage<DispatchBehaviorRewardMessage> buildEventMessage(DispatchBehaviorRewardMessage data) {
        return EventMessage.<DispatchBehaviorRewardMessage>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisTime(true))
                .data(data)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispatchBehaviorRewardMessage {

        private String userId;

        private String bizId;

        private String orderId;

        private RewardType rewardType;

        private String rewardValue;

    }
}

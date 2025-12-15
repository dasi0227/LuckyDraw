package com.dasi.domain.behavior.event;

import com.dasi.properties.TopicProperties;
import com.dasi.domain.behavior.model.type.RewardType;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DispatchBehaviorRewardEvent extends BaseEvent<DispatchBehaviorRewardEvent.DispatchBehaviorRewardMessage> {

    private final String topic;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    public DispatchBehaviorRewardEvent(TopicProperties topicProperties) {
        this.topic = topicProperties.getDispatchBehaviorReward();
    }

    @Override
    public EventMessage<DispatchBehaviorRewardMessage> buildEventMessage(DispatchBehaviorRewardMessage data) {
        return EventMessage.<DispatchBehaviorRewardMessage>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisMoment(true))
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

        private Long activityId;

        private RewardType rewardType;

        private String rewardValue;

    }
}

package com.dasi.domain.award.event;

import com.dasi.properties.TopicProperties;
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
public class DispatchActivityAwardEvent extends BaseEvent<DispatchActivityAwardEvent.DispatchActivityAwardMessage> {

    private final String topic;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    public DispatchActivityAwardEvent(TopicProperties topicProperties) {
        this.topic = topicProperties.getDispatchActivityAward();
    }

    @Override
    public EventMessage<DispatchActivityAwardMessage> buildEventMessage(DispatchActivityAwardMessage data) {
        return EventMessage.<DispatchActivityAwardMessage>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisMoment(true))
                .data(data)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispatchActivityAwardMessage {

        private String userId;

        private String orderId;

        private Long awardId;

        private Long activityId;

    }

}

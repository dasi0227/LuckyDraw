package com.dasi.domain.award.event;

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
public class DispatchActivityAwardEvent extends BaseEvent<DispatchActivityAwardEvent.DispatchActivityAwardMessage> {

    @Value("${spring.rabbitmq.topic.dispatch_raffle_award}")
    private String topic;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public EventMessage<DispatchActivityAwardMessage> buildEventMessage(DispatchActivityAwardMessage data) {
        return EventMessage.<DispatchActivityAwardMessage>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisTime(true))
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

    }

}

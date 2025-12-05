package com.dasi.domain.activity.event;

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
public class DistributeActivityAwardEvent extends BaseEvent<DistributeActivityAwardEvent.DistributeActivityAwardMessage> {

    @Value("${spring.rabbitmq.topic.distribute_raffle_award}")
    private String topic;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public EventMessage<DistributeActivityAwardMessage> buildEventMessage(DistributeActivityAwardMessage data) {
        return EventMessage.<DistributeActivityAwardMessage>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisTime(true))
                .data(data)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistributeActivityAwardMessage {

        private String userId;

        private Long awardId;

        private String orderId;

    }

}

package com.dasi.domain.activity.event;

import com.dasi.properties.TopicProperties;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.event.BaseEvent;
import com.dasi.util.TimeUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RechargeSkuStockEmptyEvent extends BaseEvent<Long> {

    private final String topic;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    public RechargeSkuStockEmptyEvent(TopicProperties topicProperties) {
        this.topic = topicProperties.getRechargeSkuStockEmpty();
    }

    @Override
    public EventMessage<Long> buildEventMessage(Long data) {
        return EventMessage.<Long>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisMoment(true))
                .data(data)
                .build();
    }

    @Override
    public String getTopic() {
        return topic;
    }
}

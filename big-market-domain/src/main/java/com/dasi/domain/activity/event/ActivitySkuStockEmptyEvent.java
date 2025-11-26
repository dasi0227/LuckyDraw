package com.dasi.domain.activity.event;

import com.dasi.types.event.BaseEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ActivitySkuStockEmptyEvent extends BaseEvent<Long> {

    @Value("${spring.rabbitmq.topic.activity_sku_stock_empty}")
    private String topic;

    @Override
    public EventMessage<Long> buildEventMessage(Long data) {
        return EventMessage.<Long>builder()
                .id(RandomStringUtils.randomNumeric(12))
                .time(LocalDateTime.now())
                .data(data)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }
}

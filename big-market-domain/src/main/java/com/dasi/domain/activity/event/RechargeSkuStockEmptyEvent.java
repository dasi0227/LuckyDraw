package com.dasi.domain.activity.event;

import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.util.TimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RechargeSkuStockEmptyEvent extends BaseEvent<Long> {

    @Value("${spring.rabbitmq.topic.recharge_sku_stock_empty}")
    private String topic;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public EventMessage<Long> buildEventMessage(Long data) {
        return EventMessage.<Long>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisTime(true))
                .data(data)
                .build();
    }

    @Override
    public String getTopic() {
        return topic;
    }
}

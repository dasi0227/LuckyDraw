package com.dasi.domain.trade.event;

import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.domain.trade.model.type.TradeType;
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
public class DispatchTradeOutcomeEvent extends BaseEvent<DispatchTradeOutcomeEvent.DispatchTradeOutcomeMessage> {

    @Value("${spring.rabbitmq.topic.dispatch_trade_outcome}")
    private String topic;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public EventMessage<DispatchTradeOutcomeMessage> buildEventMessage(DispatchTradeOutcomeMessage data) {
        return EventMessage.<DispatchTradeOutcomeMessage>builder()
                .messageId(uniqueIdGenerator.nextMessageId())
                .time(TimeUtil.thisTime(true))
                .data(data)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispatchTradeOutcomeMessage {

        private String userId;

        private String orderId;

        private Long tradeId;

        private TradeType tradeType;

    }

}
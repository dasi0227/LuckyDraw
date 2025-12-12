package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.point.event.DispatchPointTradeOutcomeEvent.DispatchTradeOutcomeMessage;
import com.dasi.domain.point.model.io.DispatchContext;
import com.dasi.domain.point.model.io.DispatchResult;
import com.dasi.domain.point.service.dispatch.IPointDispatch;
import com.dasi.types.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchTradeOutcome {

    @Resource
    private IPointDispatch pointDispatch;

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.dispatch_trade_outcome}"))
    public void dispatchTradeOutcome(String message) {
        try {
            BaseEvent.EventMessage<DispatchTradeOutcomeMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<DispatchTradeOutcomeMessage>>() {}.getType());
            DispatchTradeOutcomeMessage dispatchTradeOutcomeMessage = eventMessage.getData();

            String userId = dispatchTradeOutcomeMessage.getUserId();
            String orderId = dispatchTradeOutcomeMessage.getOrderId();
            Long tradeId = dispatchTradeOutcomeMessage.getTradeId();
            Long activityId = dispatchTradeOutcomeMessage.getActivityId();

            DispatchContext dispatchContext = DispatchContext.builder()
                    .userId(userId)
                    .tradeId(tradeId)
                    .orderId(orderId)
                    .activityId(activityId)
                    .build();
            DispatchResult dispatchResult = pointDispatch.doPointDispatch(dispatchContext);
            log.debug("{}", dispatchResult);
        } catch (Exception e) {
            log.error("【分发交易结果失败】", e);
        }

    }
}

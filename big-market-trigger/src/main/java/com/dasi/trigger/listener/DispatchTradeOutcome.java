package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.point.event.DispatchTradeOutcomeEvent.DispatchTradeOutcomeMessage;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.io.ConvertContext;
import com.dasi.domain.point.model.type.TradeState;
import com.dasi.domain.point.model.type.TradeType;
import com.dasi.domain.point.service.convert.IPointConvert;
import com.dasi.domain.point.service.trade.IPointTrade;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchTradeOutcome {

    @Resource
    private IPointTrade pointTrade;

    @Resource
    private IPointConvert pointConvert;

    @RabbitListener(queuesToDeclare = @Queue(value = "${spring.rabbitmq.topic.dispatch_trade_outcome}"))
    public void dispatchTradeOutcome(String message) {

        // 1. 解析消息
        BaseEvent.EventMessage<DispatchTradeOutcomeMessage> eventMessage = JSON.parseObject(message, new TypeReference<BaseEvent.EventMessage<DispatchTradeOutcomeMessage>>() {}.getType());
        DispatchTradeOutcomeMessage dispatchTradeOutcomeMessage = eventMessage.getData();
        String userId = dispatchTradeOutcomeMessage.getUserId();
        String orderId = dispatchTradeOutcomeMessage.getOrderId();
        Long tradeId = dispatchTradeOutcomeMessage.getTradeId();

        TradeOrderEntity tradeOrderEntity = TradeOrderEntity.builder().userId(userId).orderId(orderId).tradeId(tradeId).build();
        TradeType tradeType = dispatchTradeOutcomeMessage.getTradeType();

        try {
            // 2. 处理兑换结果
            ConvertContext convertContext = ConvertContext.builder().userId(userId).tradeId(tradeId).orderId(orderId).build();
            switch (tradeType) {
                case CONVERT_AWARD:
                    pointConvert.doConvertAward(convertContext);
                    break;
                case CONVERT_RAFFLE:
                    pointConvert.doConvertRaffle(convertContext);
                    break;
                default:
                    throw new AppException("兑换类型不存在：tradeType=" + tradeType);
            }
            // 3. 改变状态
            tradeOrderEntity.setTradeState(TradeState.USED);
            pointTrade.updateTradeOrderState(tradeOrderEntity);
        } catch (AppException e) {
            tradeOrderEntity.setTradeState(TradeState.CANCELLED);
            pointTrade.updateTradeOrderState(tradeOrderEntity);
            log.debug("【业务异常】", e);
        } catch (Exception e) {
            tradeOrderEntity.setTradeState(TradeState.CANCELLED);
            pointTrade.updateTradeOrderState(tradeOrderEntity);
            log.error("【系统异常】", e);
        }

    }
}

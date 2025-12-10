package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.award.event.DispatchActivityAwardEvent.DispatchActivityAwardMessage;
import com.dasi.domain.award.model.io.DispatchContext;
import com.dasi.domain.award.model.io.DispatchResult;
import com.dasi.domain.award.service.dispatch.IAwardDispatch;
import com.dasi.types.event.BaseEvent.EventMessage;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DispatchActivityAward {

    @Resource
    private IAwardDispatch awardDispatch;

    @RabbitListener(queuesToDeclare = @Queue(value = "dispatch_raffle_award"))
    public void dispatchActivityAward(String message) {
        try {
            EventMessage<DispatchActivityAwardMessage> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<DispatchActivityAwardMessage>>() {}.getType());
            DispatchActivityAwardMessage dispatchActivityAwardMessage = eventMessage.getData();

            String userId = dispatchActivityAwardMessage.getUserId();
            String orderId = dispatchActivityAwardMessage.getOrderId();
            Long awardId = dispatchActivityAwardMessage.getAwardId();

            DispatchContext dispatchContext = DispatchContext.builder()
                    .userId(userId)
                    .orderId(orderId)
                    .awardId(awardId)
                    .build();
            // TODO：通过websocket发送结果
            DispatchResult dispatchResult = awardDispatch.doAwardDispatch(dispatchContext);
        } catch (AppException e) {
            log.debug("【业务异常】", e);
        } catch (Exception e) {
            log.error("【系统异常】", e);
        }
    }

}
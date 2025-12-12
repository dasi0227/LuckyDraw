package com.dasi.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dasi.domain.activity.service.stock.IActivityStock;
import com.dasi.types.event.BaseEvent.EventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ClearRechargeSkuStock {

    @Resource
    private IActivityStock activityStock;

    @RabbitListener(queuesToDeclare = @Queue(value = "activity_sku_stock_empty"))
    public void clearRechargeSkuStock(String message) {
        try {
            EventMessage<Long> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<Long>>() {}.getType());
            Long skuId = eventMessage.getData();
            activityStock.clearRechargeSkuStock(skuId);
            activityStock.clearQueueValue();
            log.info("【库存】清空充值权益库存成功：skuId={}", skuId);
        } catch (Exception e) {
            log.error("【库存】清空充值权益库存失败：error={}", e.getMessage());
            throw e;
        }
    }

}

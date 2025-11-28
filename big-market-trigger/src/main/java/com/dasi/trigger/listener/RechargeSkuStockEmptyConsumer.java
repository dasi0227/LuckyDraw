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
public class RechargeSkuStockEmptyConsumer {

    @Resource
    private IActivityStock activityStock;

    @RabbitListener(queuesToDeclare = @Queue(value = "activity_sku_stock_empty"))
    public void listen(String message) {
        try {
            EventMessage<Long> eventMessage = JSON.parseObject(message, new TypeReference<EventMessage<Long>>() {}.getType());
            Long sku = eventMessage.getData();
            activityStock.clearRechargeSkuStock(sku);
            activityStock.clearQueueValue();
            log.info("【消息队列 - Consumer】接收消息，清空 SKU 库存成功：sku = {}", sku);
        } catch (Exception e) {
            log.info("【消息队列 - Consumer】接收消息，清空 SKU 库存失败：message = {}", message);
            throw e;
        }
    }

}

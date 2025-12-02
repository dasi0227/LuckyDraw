package com.dasi.trigger.job;

import com.dasi.domain.activity.model.queue.RechargeSkuStock;
import com.dasi.domain.activity.service.stock.IActivityStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class UpdateRechargeSkuStockJob {

    @Resource
    private IActivityStock activityStock;

    @Scheduled(cron = "0/5 * * * * ?")
    public void updateRechargeSkuStock() {
        try {
            RechargeSkuStock rechargeSkuStock = activityStock.getQueueValue();
            if (rechargeSkuStock != null) {
                activityStock.updateRechargeSkuStock(rechargeSkuStock.getSkuId());
                log.info("【更新活动充值权益库存】成功：skuId={}, activityId={}", rechargeSkuStock.getSkuId(), rechargeSkuStock.getActivityId());
            } else {
                log.debug("【更新活动充值权益库存】无待更新");
            }
        } catch (Exception e) {
            log.error("【更新活动充值权益库存】失败：error={}", e.getMessage());
        }
    }

}

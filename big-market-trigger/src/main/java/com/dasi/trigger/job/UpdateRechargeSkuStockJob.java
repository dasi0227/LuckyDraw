package com.dasi.trigger.job;

import com.dasi.domain.activity.model.entity.RechargeSkuStockEntity;
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
            RechargeSkuStockEntity rechargeSkuStockEntity = activityStock.getQueueValue();
            if (rechargeSkuStockEntity != null) {
                activityStock.updateRechargeSkuStock(rechargeSkuStockEntity.getSkuId());
                log.info("【定时任务 - updateRechargeSkuStock】更新活动充值权益库存成功：skuId = {}, activityId = {}", rechargeSkuStockEntity.getSkuId(), rechargeSkuStockEntity.getActivityId());
            } else {
                log.debug("【定时任务 - updateRechargeSkuStock】暂时没有活动充值权益库存待更新");
            }
        } catch (Exception e) {
            log.error("【定时任务 - updateRechargeSkuStock】更新活动充值权益库存失败：error = {}", e.getMessage());
        }
    }

}

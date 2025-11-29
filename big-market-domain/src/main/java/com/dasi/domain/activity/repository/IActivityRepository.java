package com.dasi.domain.activity.repository;

import com.dasi.domain.activity.model.aggregate.RaffleOrderAggregate;
import com.dasi.domain.activity.model.entity.RechargeSkuStockEntity;
import com.dasi.domain.activity.model.entity.*;

import java.time.LocalDateTime;
import java.util.List;

public interface IActivityRepository {

    RechargeSkuEntity queryRechargeSkuBySkuId(Long skuId);

    ActivityEntity queryActivityByActivityId(Long activityId);

    RechargeQuotaEntity queryRechargeQuotaByQuotaId(Long quotaId);

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day);

    RaffleOrderEntity queryUnusedRaffleOrder(String userId, Long activityId);

    void cacheRechargeSkuStockSurplus(Long skuId, Integer stockSurplus);

    Long subtractRechargeSkuStockSurplus(Long skuId, LocalDateTime endTime);

    void sendRechargeSkuStockConsumeToMQ(RechargeSkuStockEntity rechargeSkuStockEntity);

    RechargeSkuStockEntity getQueueValue();

    void clearQueueValue();

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

    void saveRaffleOrder(RaffleOrderAggregate raffleOrderAggregate);

    void saveRechargeOrder(RechargeOrderEntity rechargeOrderEntity);

    List<RechargeSkuEntity> queryRechargeSkuByActivityId(Long activityId);
}

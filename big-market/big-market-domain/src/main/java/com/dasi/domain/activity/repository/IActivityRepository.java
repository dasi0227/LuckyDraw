package com.dasi.domain.activity.repository;

import com.dasi.domain.activity.model.entity.*;
import com.dasi.domain.activity.model.io.ActivitySkuStock;
import com.dasi.domain.activity.model.vo.AccountSnapshot;
import com.dasi.domain.activity.model.vo.ActivitySnapshot;

import java.time.LocalDateTime;
import java.util.List;

public interface IActivityRepository {

    ActivitySkuEntity queryRechargeSkuBySkuId(Long skuId);

    ActivityEntity queryActivityByActivityId(Long activityId);

    ActivityAccountEntity queryActivityAccount(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonth(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDay(String userId, Long activityId, String day);

    AccountSnapshot queryAccountSnapshot(String userId, Long activityId);

    ActivitySnapshot queryActivitySnapshot(Long activityId);

    RaffleOrderEntity queryUnusedRaffleOrder(String userId, Long activityId);

    void cacheRechargeSkuStockSurplus(Long skuId, Integer stockSurplus);

    Long subtractRechargeSkuStockSurplus(Long skuId, LocalDateTime activityEndTime);

    void sendActivitySkuStockConsumeToMQ(ActivitySkuStock activitySkuStock);

    ActivitySkuStock getQueueValue();

    void clearQueueValue();

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

    void saveRaffleOrder(RaffleOrderEntity raffleOrderEntity);

    void saveRechargeOrder(RechargeOrderEntity rechargeOrderEntity);

    List<ActivitySkuEntity> queryRechargeSkuByActivityId(Long activityId);

    void createActivityAccountIfAbsent(String userId, Long activityId);

    Integer increaseActivityAccountLuck(ActivityAccountEntity activityAccountEntity);

    List<ActivityEntity> queryActivityList();

}

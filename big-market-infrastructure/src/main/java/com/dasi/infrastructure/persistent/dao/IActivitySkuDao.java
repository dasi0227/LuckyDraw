package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivitySku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IActivitySkuDao {

    ActivitySku queryRechargeSkuBySkuId(Long skuId);

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

    List<ActivitySku> queryRechargeSkuByActivityId(Long activityId);
}

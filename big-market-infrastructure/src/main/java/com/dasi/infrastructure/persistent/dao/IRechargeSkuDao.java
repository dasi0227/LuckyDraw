package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.RechargeSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IRechargeSkuDao {

    RechargeSku queryRechargeSkuBySkuId(Long skuId);

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);

    List<RechargeSku> queryRechargeSkuByActivityId(Long activityId);
}

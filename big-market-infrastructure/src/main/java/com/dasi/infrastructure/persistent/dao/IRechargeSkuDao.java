package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.RechargeSku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRechargeSkuDao {

    RechargeSku queryRechargeSkuBySku(Long skuId);

    void updateRechargeSkuStock(Long skuId);

    void clearRechargeSkuStock(Long skuId);
}
